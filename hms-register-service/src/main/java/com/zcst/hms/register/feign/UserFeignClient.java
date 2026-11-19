package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.UserDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务远程调用客户端
 */
@FeignClient(value = "HMS-USER-SERVICE", fallback = UserFeignFallback.class)
public interface UserFeignClient {

    @GetMapping("/api/user/internal/{id}")
    Result<UserDTO> getUser(@PathVariable("id") Long id);
}
