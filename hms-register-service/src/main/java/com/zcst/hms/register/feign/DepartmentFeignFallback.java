package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.DoctorDTO;
import com.zcst.hms.common.dto.UserDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * 科室服务熔断降级实现
 */
@Component
public class DepartmentFeignFallback implements DepartmentFeignClient {

    @Override
    public Result<DoctorDTO> getDoctor(String doctorCode) {
        return Result.fail(603, "科室服务暂时不可用，已触发熔断降级");
    }

    @Override
    public Result<UserDTO> getDept(String deptCode) {
        return Result.fail(603, "科室服务暂时不可用，已触发熔断降级");
    }
}
