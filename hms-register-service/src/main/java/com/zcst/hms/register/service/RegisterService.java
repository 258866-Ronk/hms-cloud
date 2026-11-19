package com.zcst.hms.register.service;

import com.zcst.hms.common.dto.DoctorDTO;
import com.zcst.hms.common.dto.MedicalRecordDTO;
import com.zcst.hms.common.dto.ScheduleDTO;
import com.zcst.hms.common.dto.UserDTO;
import com.zcst.hms.common.result.Result;
import com.zcst.hms.register.entity.Registration;
import com.zcst.hms.register.feign.DepartmentFeignClient;
import com.zcst.hms.register.feign.RecordFeignClient;
import com.zcst.hms.register.feign.ScheduleFeignClient;
import com.zcst.hms.register.feign.UserFeignClient;
import com.zcst.hms.register.mapper.RegistrationMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挂号业务：跨服务编排（排班扣号源 + 用户校验 + 医生信息 + 病历建档）
 */
@Service
public class RegisterService {

    private final RegistrationMapper registrationMapper;
    private final ScheduleFeignClient scheduleFeignClient;
    private final UserFeignClient userFeignClient;
    private final DepartmentFeignClient departmentFeignClient;
    private final RecordFeignClient recordFeignClient;

    public RegisterService(RegistrationMapper registrationMapper,
                           ScheduleFeignClient scheduleFeignClient,
                           UserFeignClient userFeignClient,
                           DepartmentFeignClient departmentFeignClient,
                           RecordFeignClient recordFeignClient) {
        this.registrationMapper = registrationMapper;
        this.scheduleFeignClient = scheduleFeignClient;
        this.userFeignClient = userFeignClient;
        this.departmentFeignClient = departmentFeignClient;
        this.recordFeignClient = recordFeignClient;
    }

    /**
     * 在线挂号主流程
     */
    @Transactional(rollbackFor = Exception.class)
    public Registration create(Long scheduleId, Long patientId, String chiefComplaint) {
        // 1. 调用排班服务校验排班与号源
        Result<ScheduleDTO> scheduleResult = scheduleFeignClient.getSchedule(scheduleId);
        if (scheduleResult == null || scheduleResult.getCode() != 200 || scheduleResult.getData() == null) {
            throw new IllegalArgumentException(scheduleResult == null ? "排班服务无响应" : scheduleResult.getMessage());
        }
        ScheduleDTO schedule = scheduleResult.getData();
        if (schedule.getStatus() != null && schedule.getStatus() == 0) {
            throw new IllegalArgumentException("该排班已停诊，无法挂号");
        }
        if (schedule.getRemainingNum() == null || schedule.getRemainingNum() <= 0) {
            throw new IllegalArgumentException("号源已约满，请选择其他排班");
        }

        // 2. 调用用户服务校验患者
        Result<UserDTO> userResult = userFeignClient.getUser(patientId);
        if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
            throw new IllegalArgumentException("患者信息不存在或用户服务不可用");
        }
        UserDTO patient = userResult.getData();

        // 3. 调用科室服务获取医生信息
        DoctorDTO doctor = null;
        Result<DoctorDTO> doctorResult = departmentFeignClient.getDoctor(schedule.getDoctorCode());
        if (doctorResult != null && doctorResult.getCode() == 200) {
            doctor = doctorResult.getData();
        }

        // 4. 重复挂号校验（幂等）
        if (registrationMapper.countDuplicate(patientId, scheduleId) > 0) {
            throw new IllegalArgumentException("您已挂过该医生的号，请勿重复挂号");
        }

        // 5. 扣减号源（远程调用，成功后再写本地库）
        Result<Boolean> deductResult = scheduleFeignClient.deduct(scheduleId);
        if (deductResult == null || deductResult.getCode() != 200 || !Boolean.TRUE.equals(deductResult.getData())) {
            throw new IllegalArgumentException(deductResult == null ? "号源扣减失败" : deductResult.getMessage());
        }

        // 6. 生成本地挂号单
        Integer maxQueue = registrationMapper.maxQueueNo(scheduleId);
        int queueNo = (maxQueue == null ? 0 : maxQueue) + 1;
        Registration registration = new Registration();
        registration.setRegNo("GH" + schedule.getWorkDate().replace("-", "") + scheduleId
                + String.format("%03d", queueNo));
        registration.setPatientId(patientId);
        registration.setPatientName(patient.getRealName());
        registration.setDoctorCode(schedule.getDoctorCode());
        registration.setDoctorName(schedule.getDoctorName());
        registration.setDeptCode(schedule.getDeptCode());
        registration.setDeptName(schedule.getDeptName());
        registration.setScheduleId(scheduleId);
        registration.setWorkDate(schedule.getWorkDate());
        registration.setPeriod(schedule.getPeriod());
        registration.setQueueNo(queueNo);
        registration.setFee(schedule.getRegFee());
        registration.setRoom(schedule.getRoom());
        registration.setStatus(0);
        registration.setChiefComplaint(chiefComplaint);
        registration.setRemark(doctor == null ? "" : doctor.getTitle());

        boolean deducted = true;
        try {
            registrationMapper.insert(registration);

            // 7. 调用病历服务同步建档（失败则回滚本地事务并补偿号源）
            MedicalRecordDTO recordDto = new MedicalRecordDTO();
            recordDto.setRegId(registration.getId());
            recordDto.setPatientId(patientId);
            recordDto.setPatientName(patient.getRealName());
            recordDto.setDoctorCode(schedule.getDoctorCode());
            recordDto.setDoctorName(schedule.getDoctorName());
            recordDto.setDeptCode(schedule.getDeptCode());
            recordDto.setDeptName(schedule.getDeptName());
            recordDto.setChiefComplaint(chiefComplaint);
            recordDto.setStatus(0);
            Result<MedicalRecordDTO> recordResult = recordFeignClient.create(recordDto);
            if (recordResult == null || recordResult.getCode() != 200) {
                throw new IllegalStateException("病历建档失败：" + (recordResult == null ? "无响应" : recordResult.getMessage()));
            }
        } catch (Exception e) {
            // 补偿：归还已扣减的号源
            if (deducted) {
                try {
                    scheduleFeignClient.restore(scheduleId);
                } catch (Exception ignore) {
                    // 补偿失败交由重试任务处理
                }
            }
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
        return registration;
    }

    /**
     * 退号：归还号源并置为已退号
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long id, Long patientId) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null) {
            throw new IllegalArgumentException("挂号单不存在");
        }
        if (patientId != null && !patientId.equals(reg.getPatientId())) {
            throw new IllegalArgumentException("只能退本人名下号源");
        }
        if (reg.getStatus() == 2) {
            throw new IllegalArgumentException("已完成就诊的号不可退");
        }
        if (reg.getStatus() == 3) {
            return true;
        }
        registrationMapper.updateStatus(id, 3);
        scheduleFeignClient.restore(reg.getScheduleId());
        return true;
    }

    public boolean callPatient(Long id) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null) {
            throw new IllegalArgumentException("挂号单不存在");
        }
        return registrationMapper.updateStatus(id, 1) > 0;
    }

    public boolean finish(Long id) {
        return registrationMapper.updateStatus(id, 2) > 0;
    }

    /**
     * 我的挂号（Hystrix 降级保护）
     */
    @HystrixCommand(fallbackMethod = "listMineFallback", commandKey = "listMine",
            threadPoolKey = "listMinePool")
    public List<Map<String, Object>> listMine(Long patientId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Registration r : registrationMapper.selectList(patientId, null, null, null, null)) {
            list.add(toMap(r));
        }
        return list;
    }

    public List<Map<String, Object>> listMineFallback(Long patientId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> tip = new HashMap<>(2);
        tip.put("tip", "挂号服务繁忙，已触发熔断降级，请稍后重试");
        list.add(tip);
        return list;
    }

    public Map<String, Object> getById(Long id) {
        Registration reg = registrationMapper.selectById(id);
        return reg == null ? null : toMap(reg);
    }

    public List<Map<String, Object>> listByDoctor(String doctorCode, String workDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Registration r : registrationMapper.selectList(null, doctorCode, null, workDate, null)) {
            list.add(toMap(r));
        }
        return list;
    }

    public Map<String, Object> stats(String workDate) {
        Map<String, Object> data = new HashMap<>(4);
        data.put("workDate", workDate);
        data.put("byDept", registrationMapper.statsByDept(workDate));
        data.put("byStatus", registrationMapper.statsByStatus(workDate));
        return data;
    }

    private Map<String, Object> toMap(Registration r) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("id", r.getId());
        map.put("regNo", r.getRegNo());
        map.put("patientId", r.getPatientId());
        map.put("patientName", r.getPatientName());
        map.put("doctorCode", r.getDoctorCode());
        map.put("doctorName", r.getDoctorName());
        map.put("deptCode", r.getDeptCode());
        map.put("deptName", r.getDeptName());
        map.put("scheduleId", r.getScheduleId());
        map.put("workDate", r.getWorkDate());
        map.put("period", r.getPeriod());
        map.put("queueNo", r.getQueueNo());
        map.put("fee", r.getFee());
        map.put("room", r.getRoom());
        map.put("status", r.getStatus());
        map.put("chiefComplaint", r.getChiefComplaint());
        map.put("createTime", r.getCreateTime());
        map.put("statusName", statusName(r.getStatus()));
        map.put("periodName", "AM".equals(r.getPeriod()) ? "上午" : "下午");
        return map;
    }

    private String statusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待就诊";
            case 1:
                return "就诊中";
            case 2:
                return "已完成";
            case 3:
                return "已退号";
            default:
                return "未知";
        }
    }
}
