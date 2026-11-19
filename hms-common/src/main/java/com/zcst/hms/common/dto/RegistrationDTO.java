package com.zcst.hms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 挂号单 DTO
 */
@Data
public class RegistrationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String regNo;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String deptCode;
    private String deptName;
    private Long scheduleId;
    private String workDate;
    private String period;
    private String periodName;
    private Integer queueNo;
    private BigDecimal fee;
    private String room;
    /** 0-待就诊 1-就诊中 2-已完成 3-已退号 */
    private Integer status;
    private String statusName;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
