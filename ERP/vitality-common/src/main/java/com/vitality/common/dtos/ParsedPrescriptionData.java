package com.vitality.common.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ParsedPrescriptionData {
    @JsonProperty("patient_name")
    private String patientName;

    @JsonProperty("patient_age")
    private String patientAge;

    @JsonProperty("doctor_name")
    private String doctorName;

    private String date;

    @JsonProperty("patient_issue")
    private String patientIssue;

    private String diagnosis;

    @JsonProperty("health_metrics")
    private Map<String, String> healthMetrics;

    private List<ParsedMedicineData> medicines;
}
