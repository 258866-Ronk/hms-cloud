package com.zcst.hms.department.controller;

import com.zcst.hms.common.dto.DepartmentDTO;
import com.zcst.hms.common.result.Result;
import com.zcst.hms.department.entity.Department;
import com.zcst.hms.department.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科室接口
 */
@RestController
@RequestMapping("/api/dept")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/list")
    public Result<List<DepartmentDTO>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(departmentService.listDepartments(keyword));
    }

    @GetMapping("/tree")
    public Result<List<DepartmentDTO>> tree() {
        return Result.ok(departmentService.listDepartments(null));
    }

    @GetMapping("/{deptCode}")
    public Result<DepartmentDTO> detail(@PathVariable("deptCode") String deptCode) {
        return Result.ok(departmentService.getDepartment(deptCode));
    }

    @PostMapping("/add")
    public Result<DepartmentDTO> add(@RequestBody Department department) {
        return Result.ok("新增科室成功", departmentService.addDepartment(department));
    }

    @PutMapping("/update")
    public Result<DepartmentDTO> update(@RequestBody Department department) {
        return Result.ok(departmentService.updateDepartment(department));
    }
}
