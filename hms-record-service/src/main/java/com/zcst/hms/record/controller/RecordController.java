package com.zcst.hms.record.controller;

import com.zcst.hms.common.dto.MedicalRecordDTO;
import com.zcst.hms.common.result.Result;
import com.zcst.hms.record.entity.MedicalRecord;
import com.zcst.hms.record.service.RecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子病历接口
 */
@RestController
@RequestMapping("/api/record")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * 创建病历（由挂号服务通过 Feign 编排调用）
     */
    @PostMapping("/create")
    public Result<MedicalRecordDTO> create(@RequestBody MedicalRecordDTO dto) {
        MedicalRecord record = new MedicalRecord();
        BeanUtils.copyProperties(dto, record);
        return Result.ok("病历创建成功", recordService.create(record));
    }

    @GetMapping("/{id}")
    public Result<MedicalRecordDTO> detail(@PathVariable("id") Long id) {
        return Result.ok(recordService.getById(id));
    }

    @GetMapping("/reg/{regId}")
    public Result<MedicalRecordDTO> byReg(@PathVariable("regId") Long regId) {
        return Result.ok(recordService.getByRegId(regId));
    }

    @GetMapping("/list")
    public Result<List<MedicalRecordDTO>> list(@RequestParam(required = false) Long patientId,
                                               @RequestParam(required = false) String doctorCode,
                                               @RequestParam(required = false) String keyword) {
        return Result.ok(recordService.list(patientId, doctorCode, keyword));
    }

    /**
     * 医生接诊：填写主诉、诊断、处方与医嘱
     */
    @PutMapping("/update")
    public Result<MedicalRecordDTO> update(@RequestBody MedicalRecordDTO dto) {
        MedicalRecord record = new MedicalRecord();
        BeanUtils.copyProperties(dto, record);
        return Result.ok("病历更新成功", recordService.update(record));
    }
}
