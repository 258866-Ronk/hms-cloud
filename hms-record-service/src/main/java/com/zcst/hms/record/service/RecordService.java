package com.zcst.hms.record.service;

import com.zcst.hms.common.dto.MedicalRecordDTO;
import com.zcst.hms.record.entity.Consultation;
import com.zcst.hms.record.entity.MedicalRecord;
import com.zcst.hms.record.mapper.RecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 电子病历 / 问诊记录业务逻辑
 */
@Service
public class RecordService {

    private final RecordMapper recordMapper;

    public RecordService(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    /**
     * 创建病历：以 reg_id 做幂等控制，避免重复挂号产生多份病历
     */
    public MedicalRecordDTO create(MedicalRecord record) {
        if (record.getRegId() != null) {
            MedicalRecord exist = recordMapper.selectByRegId(record.getRegId());
            if (exist != null) {
                return toDto(exist);
            }
        }
        if (record.getRecordNo() == null || record.getRecordNo().isEmpty()) {
            record.setRecordNo("MR" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        }
        if (record.getStatus() == null) {
            record.setStatus(0);
        }
        recordMapper.insert(record);
        return toDto(recordMapper.selectById(record.getId()));
    }

    public MedicalRecordDTO getById(Long id) {
        MedicalRecord r = recordMapper.selectById(id);
        return r == null ? null : toDto(r);
    }

    public MedicalRecordDTO getByRegId(Long regId) {
        MedicalRecord r = recordMapper.selectByRegId(regId);
        return r == null ? null : toDto(r);
    }

    public List<MedicalRecordDTO> list(Long patientId, String doctorCode, String keyword) {
        List<MedicalRecordDTO> result = new ArrayList<>();
        for (MedicalRecord r : recordMapper.selectList(patientId, doctorCode, keyword)) {
            result.add(toDto(r));
        }
        return result;
    }

    /**
     * 医生接诊：填写诊断、处方与医嘱
     */
    public MedicalRecordDTO update(MedicalRecord record) {
        recordMapper.update(record);
        return getById(record.getId());
    }

    public List<Consultation> consultations(Long regId) {
        return recordMapper.selectConsultations(regId);
    }

    public Consultation addConsultation(Consultation consultation) {
        recordMapper.insertConsultation(consultation);
        return consultation;
    }

    private MedicalRecordDTO toDto(MedicalRecord r) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        BeanUtils.copyProperties(r, dto);
        dto.setStatusName(r.getStatus() != null && r.getStatus() == 1 ? "已完成" : "问诊中");
        return dto;
    }
}
