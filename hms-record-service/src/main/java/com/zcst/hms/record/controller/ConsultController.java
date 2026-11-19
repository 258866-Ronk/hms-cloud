package com.zcst.hms.record.controller;

import com.zcst.hms.common.result.Result;
import com.zcst.hms.record.entity.Consultation;
import com.zcst.hms.record.service.RecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线问诊对话接口
 */
@RestController
@RequestMapping("/api/consult")
public class ConsultController {

    private final RecordService recordService;

    public ConsultController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/add")
    public Result<Consultation> add(@RequestBody Consultation consultation) {
        return Result.ok(recordService.addConsultation(consultation));
    }

    @GetMapping("/list/{regId}")
    public Result<List<Consultation>> list(@PathVariable("regId") Long regId) {
        return Result.ok(recordService.consultations(regId));
    }
}
