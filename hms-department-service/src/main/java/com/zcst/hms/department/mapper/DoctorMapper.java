package com.zcst.hms.department.mapper;

import com.zcst.hms.department.entity.Doctor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医生数据访问接口
 */
public interface DoctorMapper {

    List<Doctor> selectList(@Param("deptCode") String deptCode, @Param("keyword") String keyword);

    Doctor selectByCode(@Param("doctorCode") String doctorCode);

    int insert(Doctor doctor);

    int updateByCode(Doctor doctor);
}
