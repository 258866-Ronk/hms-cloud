package com.zcst.hms.schedule.mapper;

import com.zcst.hms.schedule.entity.Schedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 排班 / 号源数据访问接口
 */
public interface ScheduleMapper {

    List<Schedule> selectList(@Param("deptCode") String deptCode,
                              @Param("doctorCode") String doctorCode,
                              @Param("workDate") String workDate,
                              @Param("onlyAvailable") boolean onlyAvailable);

    Schedule selectById(@Param("id") Long id);

    /** 悲观锁查询：SELECT ... FOR UPDATE */
    Schedule selectByIdForUpdate(@Param("id") Long id);

    /** 原子扣减号源，依赖 remaining_num > 0 条件，避免超卖 */
    int deductRemaining(@Param("id") Long id);

    /** 归还号源 */
    int restoreRemaining(@Param("id") Long id);

    int insert(Schedule schedule);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 号源统计：按科室汇总 */
    List<Map<String, Object>> statsByDept(@Param("workDate") String workDate);
}
