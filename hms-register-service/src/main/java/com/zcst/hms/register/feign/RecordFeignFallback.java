package com.zcst.hms.register.feign;

import com.zcst.hms.common.dto.MedicalRecordDTO;
import com.zcst.hms.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * 病历服务熔断降级实现
 */
@Component
public class RecordFeignFallback implements RecordFeignClient {

    @Override
    public Result<MedicalRecordDTO> create(MedicalRecordDTO record) {
        return Result.fail(603, "病历服务暂时不可用，已触发熔断降级，病历将由补偿任务补建");
    }

    @Override
    public Result<MedicalRecordDTO> getByRegId(Long regId) {
        return Result.fail(603, "病历服务暂时不可用，已触发熔断降级");
    }
}
