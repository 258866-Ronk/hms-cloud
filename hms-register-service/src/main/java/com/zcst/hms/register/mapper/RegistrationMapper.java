package com.zcst.hms.register.mapper;

import com.zcst.hms.register.entity.Registration;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 挂号数据访问接口
 */
public interface RegistrationMapper {

    Registration selectById(@Param("id") Long id);

    List<Registration> selectList(@Param("patientId") Long patientId,
                                  @Param("doctorCode") String doctorCode,
                                  @Param("deptCode") String deptCode,
                                  @Param("workDate") String workDate,
                                  @Param("status") Integer status);

    int insert(Registration registration);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateRemark(@Param("id") Long id, @Param("remark") String remark);

    /** 重复挂号校验 */
    int countDuplicate(@Param("patientId") Long patientId, @Param("scheduleId") Long scheduleId);

    /** 当前排班已取到的最大就诊序号 */
    Integer maxQueueNo(@Param("scheduleId") Long scheduleId);

    /** 今日各科室挂号量 */
    List<Map<String, Object>> statsByDept(@Param("workDate") String workDate);

    /** 各状态挂号数 */
    List<Map<String, Object>> statsByStatus(@Param("workDate") String workDate);
}
