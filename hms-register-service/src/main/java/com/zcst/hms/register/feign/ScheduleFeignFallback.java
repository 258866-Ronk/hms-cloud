package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.ScheduleDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * 排班服务熔断降级实现
 */
@Component
public class ScheduleFeignFallback implements ScheduleFeignClient {

    @Override
    public Result<ScheduleDTO> getSchedule(Long id) {
        return Result.fail(603, "排班服务暂时不可用，已触发熔断降级");
    }

    @Override
    public Result<Boolean> deduct(Long id) {
        return Result.fail(603, "排班服务暂时不可用，挂号失败，请稍后重试");
    }

    @Override
    public Result<Boolean> restore(Long id) {
        return Result.fail(603, "排班服务暂时不可用，号源归还失败，将由补偿任务重试");
    }
}
