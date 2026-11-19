package com.zcst.hms.department.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 医生实体
 */
@Data
public class Doctor {

    private Long id;
    private String doctorCode;
    private String doctorName;
    private String deptCode;
    private String deptName;
    private String title;
    private String specialty;
    private String intro;
    private BigDecimal regFee;
    private Integer gender;
    private Integer status;
    private Date createTime;
}
