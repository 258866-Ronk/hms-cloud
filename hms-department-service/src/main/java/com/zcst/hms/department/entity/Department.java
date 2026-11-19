package com.zcst.hms.department.entity;

import lombok.Data;

import java.util.Date;

/**
 * 科室实体
 */
@Data
public class Department {

    private Long id;
    private String deptCode;
    private String deptName;
    private String parentCode;
    private String location;
    private String description;
    private Integer doctorCount;
    private Integer sort;
    private Integer status;
    private Date createTime;
}
