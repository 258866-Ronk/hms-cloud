package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.ScheduleDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 排班服务远程调用客户端（fallback 指向熔断降级实现）
 */
@FeignClient(value = "HMS-SCHEDULE-SERVICE", fallback = ScheduleFeignFallback.class)
public interface ScheduleFeignClient {

    @GetMapping("/api/schedule/internal/{id}")
    Result<ScheduleDTO> getSchedule(@PathVariable("id") Long id);

    @PostMapping("/api/schedule/internal/deduct/{id}")
    Result<Boolean> deduct(@PathVariable("id") Long id);

    @PostMapping("/api/schedule/internal/restore/{id}")
    Result<Boolean> restore(@PathVariable("id") Long id);
}
