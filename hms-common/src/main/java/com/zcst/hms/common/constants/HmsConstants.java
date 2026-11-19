package com.zcst.hms.common.constants;

/**
 * 系统全局常量
 */
public interface HmsConstants {

    /** 令牌请求头 */
    String HEADER_TOKEN = "Authorization";
    /** 网关透传的用户 ID */
    String HEADER_USER_ID = "X-User-Id";
    /** 网关透传的用户名 */
    String HEADER_USERNAME = "X-Username";
    /** 网关透传的角色 */
    String HEADER_ROLE = "X-Role";
    /** 网关透传的真实姓名 */
    String HEADER_REAL_NAME = "X-Real-Name";

    String ROLE_PATIENT = "PATIENT";
    String ROLE_DOCTOR = "DOCTOR";
    String ROLE_ADMIN = "ADMIN";

    /** 挂号状态 */
    Integer REG_WAITING = 0;
    Integer REG_VISITING = 1;
    Integer REG_FINISHED = 2;
    Integer REG_CANCELLED = 3;

    /** 出诊时段 */
    String PERIOD_AM = "AM";
    String PERIOD_PM = "PM";
}
