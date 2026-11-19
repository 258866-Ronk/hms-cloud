package com.zcst.hms.register.controller;

import com.zcst.hms.common.constants.HmsConstants;
import com.zcst.hms.common.result.Result;
import com.zcst.hms.register.entity.Registration;
import com.zcst.hms.register.service.RegisterService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 挂号接口
 */
@RestController
@RequestMapping("/api/register")
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    /**
     * 在线挂号（重点接口，网关侧配置 10 次/秒 限流）
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body,
                                              HttpServletRequest request) {
        Long scheduleId = Long.valueOf(String.valueOf(body.get("scheduleId")));
        Long patientId = body.get("patientId") == null
                ? Long.valueOf(request.getHeader(HmsConstants.HEADER_USER_ID))
                : Long.valueOf(String.valueOf(body.get("patientId")));
        String chiefComplaint = body.get("chiefComplaint") == null ? null : String.valueOf(body.get("chiefComplaint"));
        Registration registration = registerService.create(scheduleId, patientId, chiefComplaint);

        Map<String, Object> data = new java.util.HashMap<>(8);
        data.put("id", registration.getId());
        data.put("regNo", registration.getRegNo());
        data.put("queueNo", registration.getQueueNo());
        data.put("doctorName", registration.getDoctorName());
        data.put("deptName", registration.getDeptName());
        data.put("workDate", registration.getWorkDate());
        data.put("period", registration.getPeriod());
        data.put("room", registration.getRoom());
        data.put("fee", registration.getFee());
        return Result.ok("挂号成功", data);
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my(HttpServletRequest request,
                                                @RequestParam(required = false) Long patientId) {
        if (patientId == null) {
            patientId = Long.valueOf(request.getHeader(HmsConstants.HEADER_USER_ID));
        }
        return Result.ok(registerService.listMine(patientId));
    }

    @GetMapping("/doctor")
    public Result<List<Map<String, Object>>> doctor(@RequestParam(required = false) String doctorCode,
                                                    @RequestParam(required = false) String workDate,
                                                    HttpServletRequest request) {
        if (doctorCode == null || doctorCode.isEmpty()) {
            doctorCode = request.getHeader(HmsConstants.HEADER_USERNAME);
        }
        return Result.ok(registerService.listByDoctor(doctorCode, workDate));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable("id") Long id) {
        return Result.ok(registerService.getById(id));
    }

    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancel(@PathVariable("id") Long id, HttpServletRequest request) {
        String userId = request.getHeader(HmsConstants.HEADER_USER_ID);
        return Result.ok("退号成功，号源已归还", registerService.cancel(id, Long.valueOf(userId)));
    }

    @PostMapping("/call/{id}")
    public Result<Boolean> call(@PathVariable("id") Long id) {
        return Result.ok("已叫号", registerService.callPatient(id));
    }

    @PostMapping("/finish/{id}")
    public Result<Boolean> finish(@PathVariable("id") Long id) {
        return Result.ok("就诊完成", registerService.finish(id));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) String workDate) {
        return Result.ok(registerService.stats(workDate));
    }
}
