package com.zcst.hms.department.service;

import com.zcst.hms.common.dto.DepartmentDTO;
import com.zcst.hms.common.dto.DoctorDTO;
import com.zcst.hms.department.entity.Department;
import com.zcst.hms.department.entity.Doctor;
import com.zcst.hms.department.mapper.DepartmentMapper;
import com.zcst.hms.department.mapper.DoctorMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 科室 / 医生业务逻辑
 */
@Service
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final DoctorMapper doctorMapper;

    public DepartmentService(DepartmentMapper departmentMapper, DoctorMapper doctorMapper) {
        this.departmentMapper = departmentMapper;
        this.doctorMapper = doctorMapper;
    }

    public List<DepartmentDTO> listDepartments(String keyword) {
        return toDeptDtoList(departmentMapper.selectAll(keyword));
    }

    public DepartmentDTO getDepartment(String deptCode) {
        Department d = departmentMapper.selectByCode(deptCode);
        return d == null ? null : toDeptDto(d);
    }

    public List<DoctorDTO> listDoctors(String deptCode, String keyword) {
        return toDoctorDtoList(doctorMapper.selectList(deptCode, keyword));
    }

    public DoctorDTO getDoctor(String doctorCode) {
        Doctor d = doctorMapper.selectByCode(doctorCode);
        return d == null ? null : toDoctorDto(d);
    }

    public DepartmentDTO addDepartment(Department department) {
        if (departmentMapper.selectByCode(department.getDeptCode()) != null) {
            throw new IllegalArgumentException("科室编码已存在");
        }
        departmentMapper.insert(department);
        return getDepartment(department.getDeptCode());
    }

    public DepartmentDTO updateDepartment(Department department) {
        departmentMapper.updateByCode(department);
        return getDepartment(department.getDeptCode());
    }

    public DoctorDTO addDoctor(Doctor doctor) {
        if (departmentMapper.selectByCode(doctor.getDeptCode()) == null) {
            throw new IllegalArgumentException("所属科室不存在");
        }
        doctorMapper.insert(doctor);
        return getDoctor(doctor.getDoctorCode());
    }

    private List<DepartmentDTO> toDeptDtoList(List<Department> list) {
        List<DepartmentDTO> result = new ArrayList<>();
        for (Department d : list) {
            result.add(toDeptDto(d));
        }
        return result;
    }

    private DepartmentDTO toDeptDto(Department d) {
        DepartmentDTO dto = new DepartmentDTO();
        BeanUtils.copyProperties(d, dto);
        return dto;
    }

    private List<DoctorDTO> toDoctorDtoList(List<Doctor> list) {
        List<DoctorDTO> result = new ArrayList<>();
        for (Doctor d : list) {
            result.add(toDoctorDto(d));
        }
        return result;
    }

    private DoctorDTO toDoctorDto(Doctor d) {
        DoctorDTO dto = new DoctorDTO();
        BeanUtils.copyProperties(d, dto);
        return dto;
    }
}
