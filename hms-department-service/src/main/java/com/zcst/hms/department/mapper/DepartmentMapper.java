package com.zcst.hms.department.mapper;

import com.zcst.hms.department.entity.Department;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 科室数据访问接口
 */
public interface DepartmentMapper {

    List<Department> selectAll(@Param("keyword") String keyword);

    Department selectByCode(@Param("deptCode") String deptCode);

    int insert(Department department);

    int updateByCode(Department department);

    int deleteByCode(@Param("deptCode") String deptCode);
}
