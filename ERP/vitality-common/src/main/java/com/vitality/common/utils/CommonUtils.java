package com.vitality.common.utils;

import com.vitality.common.dtos.CreatePrescriptionDiagnosisRequest;
import com.vitality.common.dtos.ParsedMedicineData;
import com.vitality.common.dtos.ParsedPrescriptionData;
import com.vitality.common.exceptions.InvalidRequestException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {
    /**
     * Method to get the unique key for an item based on item description, batch number and expiry date.
     *
     * @param itemDesc:    the item description.
     * @param batchNumber: the batch number.
     * @param expiryDate:  the expiry date.
     * @return the unique key for the item.
     */
    public static String getUniqueItemKey(String itemDesc, String batchNumber, LocalDate expiryDate) {
        if (StringUtils.hasLength(itemDesc) && StringUtils.hasLength(batchNumber) && expiryDate != null) {
            return itemDesc.trim().toLowerCase() + "_" + batchNumber.trim().toLowerCase() + "_" + expiryDate;
        }
        return null;
    }

    public static List<CreatePrescriptionDiagnosisRequest> toDiagnosisRequests(ParsedPrescriptionData data) {
        List<CreatePrescriptionDiagnosisRequest> requests = new ArrayList<>();
        if (data.getMedicines() == null) {
            return requests;
        }
        LocalDate startDate = parseDate(data.getDate());
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        for (ParsedMedicineData medicine : data.getMedicines()) {
            if (medicine == null || ObjectUtils.isEmpty(medicine.getName())) {
                continue;
            }
            CreatePrescriptionDiagnosisRequest request = new CreatePrescriptionDiagnosisRequest();
            request.setDiagnosis(data.getDiagnosis());
            request.setMedicineName(medicine.getName());
            request.setDosage(medicine.getDosage());
            request.setUnit(medicine.getQuantity() == null ? null : BigDecimal.valueOf(medicine.getQuantity()));
            request.setUnitMeasure(medicine.getQuantity() == null ? null : "unit");
            request.setStartDate(startDate);
            requests.add(request);
        }
        return requests;
    }

    public static String[] splitPatientName(String patientName) {
        if (ObjectUtils.isEmpty(patientName)) {
            throw new InvalidRequestException("Patient name is required.");
        }
        String trimmed = patientName.trim();
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex < 0) {
            return new String[]{trimmed, null};
        }
        return new String[]{trimmed.substring(0, spaceIndex), trimmed.substring(spaceIndex + 1).trim()};
    }

    public static Integer parseInteger(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static LocalDate parseDate(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatHealthMetrics(Map<String, String> healthMetrics) {
        if (healthMetrics == null || healthMetrics.isEmpty()) {
            return null;
        }
        List<String> entries = new ArrayList<>();
        healthMetrics.forEach((key, value) -> entries.add(key + ": " + value));
        return String.join(", ", entries);
    }

    public static String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return GuidUtils.generateGuid() + ".jpg";
        }
        return Paths.get(originalFilename).getFileName().toString();
    }

    /**
     * Method to get the next six month date from the current date.
     *
     * @return the next six month date from the current date.
     */
    public static LocalDate getNextSixMonthDate() {
        return LocalDate.now().plusMonths(6);
    }
}
