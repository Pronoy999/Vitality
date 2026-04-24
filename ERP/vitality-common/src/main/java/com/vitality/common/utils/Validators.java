package com.vitality.common.utils;

import com.vitality.common.dtos.CreateInvoiceRequest;
import com.vitality.common.dtos.CreatePrescriptionDiagnosisRequest;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.dtos.InvoiceItemsRequest;
import com.vitality.common.exceptions.InvalidRequestException;

import java.util.List;
import java.util.Objects;

public class Validators {
    public static void validatePrescriptionDiagnosis(CreatePrescriptionRequest request) {
        List<CreatePrescriptionDiagnosisRequest> diagnosisRequest = request.getPrescriptionDiagnoses();
        if (diagnosisRequest == null || diagnosisRequest.isEmpty()) {
            throw new InvalidRequestException("At least one prescription diagnosis is required");
        }
        for (CreatePrescriptionDiagnosisRequest diagnosis : diagnosisRequest) {
            if (diagnosis.getMedicineName() == null || diagnosis.getMedicineName().isBlank()) {
                throw new InvalidRequestException("Medicine name cannot be blank");
            }
            if (diagnosis.getStartDate() == null) {
                throw new InvalidRequestException("Start date is required");
            }
        }
    }

    /**
     * Method to validate the Invoice Create Request.
     *
     * @param request: the Request to validate.
     */
    public static void validateInvoiceItems(CreateInvoiceRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getTotalPrice())) {
            throw new InvalidRequestException("Invoice request and total price cannot be null");
        }
        List<InvoiceItemsRequest> invoiceItems = request.getInvoiceItems();
        if (invoiceItems == null || invoiceItems.isEmpty()) {
            throw new InvalidRequestException("Invoice items cannot be empty");
        }
        invoiceItems.forEach(item -> {
            if (item.getItemDescription().isBlank() || item.getBatchNumber().isBlank() || Objects.isNull(item.getExpiryDate()) ||
                    Objects.isNull(item.getItemTotalPrice()) || Objects.isNull(item.getMrp()) || Objects.isNull(item.getItemPrice())) {
                throw new InvalidRequestException("Item description and batch number cannot be blank");
            }
        });
    }
}
