package com.zcst.hms.register.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 挂号单实体
 */
@Data
public class Registration {

    private Long id;
    private String regNo;
    private Long patientId;
    private String patientName;
    private String doctorCode;
    private String doctorName;
    private String deptCode;
    private String deptName;
    private Long scheduleId;
    private String workDate;
    private String period;
    private Integer queueNo;
    private BigDecimal fee;
    private String room;
    /** 0-待就诊 1-就诊中 2-已完成 3-已退号 */
    private Integer status;
    private String chiefComplaint;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
