package com.zcst.hms.record.mapper;

import com.zcst.hms.record.entity.Consultation;
import com.zcst.hms.record.entity.MedicalRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 病历 / 问诊记录数据访问接口
 */
public interface RecordMapper {

    List<MedicalRecord> selectList(@Param("patientId") Long patientId,
                                   @Param("doctorCode") String doctorCode,
                                   @Param("keyword") String keyword);

    MedicalRecord selectById(@Param("id") Long id);

    MedicalRecord selectByRegId(@Param("regId") Long regId);

    int insert(MedicalRecord record);

    int update(MedicalRecord record);

    List<Consultation> selectConsultations(@Param("regId") Long regId);

    int insertConsultation(Consultation consultation);
}
