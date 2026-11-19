package com.zcst.hms.record.entity;

import lombok.Data;

import java.util.Date;

/**
 * 电子病历实体
 */
@Data
public class MedicalRecord {

    private Long id;
    private String recordNo;
    private Long regId;
    private Long patientId;
    private String patientName;
    private String doctorCode;
    private String doctorName;
    private String deptCode;
    private String deptName;
    /** 主诉 */
    private String chiefComplaint;
    /** 诊断结果 */
    private String diagnosis;
    /** 处方 */
    private String prescription;
    /** 医嘱 */
    private String advice;
    /** 0-问诊中 1-已完成 */
    private Integer status;
    private Date visitTime;
    private Date createTime;
    private Date updateTime;
}
