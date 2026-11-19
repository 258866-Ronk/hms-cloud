package com.zcst.hms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 科室 DTO
 */
@Data
public class DepartmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
