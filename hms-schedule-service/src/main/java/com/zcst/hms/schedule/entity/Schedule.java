package com.zcst.hms.schedule.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 排班 / 号源实体
 */
@Data
public class Schedule {

    private Long id;
    private String doctorCode;
    private String doctorName;
    private String deptCode;
    private String deptName;
    /** 出诊日期 yyyy-MM-dd */
    private String workDate;
    /** AM / PM */
    private String period;
    private Integer totalNum;
    private Integer remainingNum;
    private BigDecimal regFee;
    private String room;
    /** 1-正常 0-停诊 */
    private Integer status;
    private Integer version;
    private Date createTime;
    private Date updateTime;
}
