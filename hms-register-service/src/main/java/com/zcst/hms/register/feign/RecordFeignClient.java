package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.MedicalRecordDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 病历服务远程调用客户端
 */
@FeignClient(value = "HMS-RECORD-SERVICE", fallback = RecordFeignFallback.class)
public interface RecordFeignClient {

    @PostMapping("/api/record/create")
    Result<MedicalRecordDTO> create(@RequestBody MedicalRecordDTO record);

    @GetMapping("/api/record/reg/{regId}")
    Result<MedicalRecordDTO> getByRegId(@PathVariable("regId") Long regId);
}
