package com.example.demo.Services;

import com.example.demo.DTOs.RequestDTO.MedicalRecordRequest;
import com.example.demo.Entities.MedicalRecord;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MedicalRecordService {
    void createMedicalRecord(MedicalRecordRequest request);
    void uploadMedicalDocuments(Integer recordId, MultipartFile[] files);
    List<MedicalRecord> getPatientHistory(Integer patientId);
}