package com.zcst.hms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电子病历 / 问诊记录 DTO
 */
@Data
public class MedicalRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String recordNo;
    private Long regId;
    private Long patientId;
    private String patientName;
    private String doctorCode;
    private String doctorName;
    private String deptCode;
    private String deptName;
    private String chiefComplaint;
    private String diagnosis;
    private String prescription;
    private String advice;
    /** 0-问诊中 1-已完成 */
    private Integer status;
    private String statusName;
    private Date visitTime;
    private Date createTime;
    private Date updateTime;
}
