package com.zcst.hms.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体（患者 / 医生 / 管理员）
 */
@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String realName;
    /** PATIENT / DOCTOR / ADMIN */
    private String role;
    private String phone;
    private String idCard;
    private Integer gender;
    private Integer age;
    private String deptCode;
    private String title;
    private String specialty;
    private String intro;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
