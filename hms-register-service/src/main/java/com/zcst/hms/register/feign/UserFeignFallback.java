package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.UserDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * 用户服务熔断降级实现
 */
@Component
public class UserFeignFallback implements UserFeignClient {

    @Override
    public Result<UserDTO> getUser(Long id) {
        return Result.fail(603, "用户服务暂时不可用，已触发熔断降级");
    }
}
