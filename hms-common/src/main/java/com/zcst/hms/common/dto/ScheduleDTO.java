package com.zcst.hms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 排班 / 号源 DTO
 */
@Data
public class ScheduleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long doctorId;
    private String doctorCode;
    private String doctorName;
    private String deptCode;
    private String deptName;
    private String workDate;
    private String period;
    private String periodName;
    private Integer totalNum;
    private Integer remainingNum;
    private BigDecimal regFee;
    private String room;
    private Integer status;
    private Integer version;
    private Date createTime;
}
