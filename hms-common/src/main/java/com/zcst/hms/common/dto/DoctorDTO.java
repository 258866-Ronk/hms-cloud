package com.zcst.hms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 医生 DTO
 */
@Data
public class DoctorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
