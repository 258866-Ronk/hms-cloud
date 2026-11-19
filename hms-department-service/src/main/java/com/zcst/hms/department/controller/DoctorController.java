package com.zcst.hms.department.controller;

import com.zcst.hms.common.dto.DoctorDTO;
import com.zcst.hms.common.result.Result;
import com.zcst.hms.department.entity.Doctor;
import com.zcst.hms.department.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生接口
 */
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DepartmentService departmentService;

    public DoctorController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/list")
    public Result<List<DoctorDTO>> list(@RequestParam(required = false) String deptCode,
                                        @RequestParam(required = false) String keyword) {
        return Result.ok(departmentService.listDoctors(deptCode, keyword));
    }

    @GetMapping("/{doctorCode}")
    public Result<DoctorDTO> detail(@PathVariable("doctorCode") String doctorCode) {
        return Result.ok(departmentService.getDoctor(doctorCode));
    }

    /**
     * 服务内部调用：供排班 / 挂号服务获取医生信息
     */
    @GetMapping("/internal/{doctorCode}")
    public Result<DoctorDTO> internal(@PathVariable("doctorCode") String doctorCode) {
        return Result.ok(departmentService.getDoctor(doctorCode));
    }

    @PostMapping("/add")
    public Result<DoctorDTO> add(@RequestBody Doctor doctor) {
        return Result.ok("新增医生成功", departmentService.addDoctor(doctor));
    }
}
