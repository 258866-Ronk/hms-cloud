package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.DoctorDTO;
import com.zcst.hms.common.dto.UserDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 科室 / 医生服务远程调用客户端
 */
@FeignClient(value = "HMS-DEPARTMENT-SERVICE", fallback = DepartmentFeignFallback.class)
public interface DepartmentFeignClient {

    @GetMapping("/api/doctor/internal/{doctorCode}")
    Result<DoctorDTO> getDoctor(@PathVariable("doctorCode") String doctorCode);

    @GetMapping("/api/dept/{deptCode}")
    Result<UserDTO> getDept(@PathVariable("deptCode") String deptCode);
}
