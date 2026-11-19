package com.zcst.hms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户 DTO（患者 / 医生 / 管理员）
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String realName;
    private String role;
    private String phone;
    private String idCard;
    private Integer gender;
    private Integer age;
    private String deptCode;
    private String title;
    private String specialty;
    private Integer status;
    private Date createTime;
}
