package com.zcst.hms.record.entity;

import lombok.Data;

import java.util.Date;

/**
 * 问诊对话记录实体
 */
@Data
public class Consultation {

    private Long id;
    private Long recordId;
    private Long regId;
    private Long patientId;
    private String doctorCode;
    /** PATIENT / DOCTOR */
    private String sender;
    private String content;
    private Date createTime;
}
