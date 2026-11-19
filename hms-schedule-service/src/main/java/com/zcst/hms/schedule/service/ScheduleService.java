package com.zcst.hms.schedule.service;

import com.zcst.hms.common.dto.ScheduleDTO;
import com.zcst.hms.schedule.entity.Schedule;
import com.zcst.hms.schedule.mapper.ScheduleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排班 / 号源业务逻辑
 */
@Service
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;

    public ScheduleService(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    public List<ScheduleDTO> list(String deptCode, String doctorCode, String workDate, boolean onlyAvailable) {
        return toDtoList(scheduleMapper.selectList(deptCode, doctorCode, workDate, onlyAvailable));
    }

    public ScheduleDTO getById(Long id) {
        Schedule s = scheduleMapper.selectById(id);
        return s == null ? null : toDto(s);
    }

    /**
     * 扣减号源：先加行锁查询校验，再执行带条件的原子更新，保证不超卖
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deduct(Long id) {
        Schedule locked = scheduleMapper.selectByIdForUpdate(id);
        if (locked == null) {
            throw new IllegalArgumentException("排班不存在");
        }
        if (locked.getStatus() != null && locked.getStatus() == 0) {
            throw new IllegalArgumentException("该排班已停诊");
        }
        if (locked.getRemainingNum() == null || locked.getRemainingNum() <= 0) {
            return false;
        }
        return scheduleMapper.deductRemaining(id) > 0;
    }

    /**
     * 归还号源（退号 / 事务回滚补偿）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean restore(Long id) {
        return scheduleMapper.restoreRemaining(id) > 0;
    }

    public ScheduleDTO add(Schedule schedule) {
        if (schedule.getRemainingNum() == null) {
            schedule.setRemainingNum(schedule.getTotalNum());
        }
        if (schedule.getRegFee() == null) {
            schedule.setRegFee(new java.math.BigDecimal("10.00"));
        }
        scheduleMapper.insert(schedule);
        return getById(schedule.getId());
    }

    public boolean stop(Long id) {
        return scheduleMapper.updateStatus(id, 0) > 0;
    }

    public List<Map<String, Object>> statsByDept(String workDate) {
        return scheduleMapper.statsByDept(workDate);
    }

    private List<ScheduleDTO> toDtoList(List<Schedule> list) {
        List<ScheduleDTO> result = new ArrayList<>();
        for (Schedule s : list) {
            result.add(toDto(s));
        }
        return result;
    }

    private ScheduleDTO toDto(Schedule s) {
        ScheduleDTO dto = new ScheduleDTO();
        BeanUtils.copyProperties(s, dto);
        dto.setPeriodName("AM".equals(s.getPeriod()) ? "上午" : "下午");
        return dto;
    }
}
