package com.zcst.hms.schedule.controller;

import com.zcst.hms.common.dto.ScheduleDTO;
import com.zcst.hms.common.result.Result;
import com.zcst.hms.schedule.entity.Schedule;
import com.zcst.hms.schedule.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排班 / 号源接口
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/list")
    public Result<List<ScheduleDTO>> list(@RequestParam(required = false) String deptCode,
                                          @RequestParam(required = false) String doctorCode,
                                          @RequestParam(required = false) String workDate,
                                          @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable) {
        return Result.ok(scheduleService.list(deptCode, doctorCode, workDate, onlyAvailable));
    }

    @GetMapping("/available")
    public Result<List<ScheduleDTO>> available(@RequestParam(required = false) String deptCode,
                                               @RequestParam(required = false) String workDate) {
        return Result.ok(scheduleService.list(deptCode, null, workDate, true));
    }

    @GetMapping("/{id}")
    public Result<ScheduleDTO> detail(@PathVariable("id") Long id) {
        return Result.ok(scheduleService.getById(id));
    }

    /**
     * 服务内部调用：供挂号服务查询排班
     */
    @GetMapping("/internal/{id}")
    public Result<ScheduleDTO> internal(@PathVariable("id") Long id) {
        return Result.ok(scheduleService.getById(id));
    }

    /**
     * 服务内部调用：扣减号源（返回 code=601 表示号源不足）
     */
    @PostMapping("/internal/deduct/{id}")
    public Result<Boolean> deduct(@PathVariable("id") Long id) {
        boolean ok = scheduleService.deduct(id);
        if (!ok) {
            return Result.fail(601, "号源不足，挂号失败");
        }
        return Result.ok(true);
    }

    /**
     * 服务内部调用：归还号源（退号 / 补偿）
     */
    @PostMapping("/internal/restore/{id}")
    public Result<Boolean> restore(@PathVariable("id") Long id) {
        return Result.ok(scheduleService.restore(id));
    }

    @PostMapping("/add")
    public Result<ScheduleDTO> add(@RequestBody Schedule schedule) {
        return Result.ok("新增排班成功", scheduleService.add(schedule));
    }

    @PostMapping("/stop/{id}")
    public Result<Boolean> stop(@PathVariable("id") Long id) {
        return Result.ok("停诊成功", scheduleService.stop(id));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) String workDate) {
        Map<String, Object> data = new HashMap<>(4);
        data.put("workDate", workDate);
        data.put("items", scheduleService.statsByDept(workDate));
        return Result.ok(data);
    }
}
