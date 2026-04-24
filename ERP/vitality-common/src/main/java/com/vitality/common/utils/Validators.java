package com.vitality.common.utils;

import com.vitality.common.dtos.CreateInvoiceRequest;
import com.vitality.common.dtos.CreatePrescriptionDiagnosisRequest;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.dtos.InvoiceItemsRequest;
import com.vitality.common.exceptions.InvalidRequestException;

import java.util.List;
import java.util.Objects;

import static org.apache.http.util.TextUtils.isBlank;

public class Validators {
    public static void validatePrescriptionDiagnosis(CreatePrescriptionRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Prescription request is required.");
        }
        List<CreatePrescriptionDiagnosisRequest> diagnosisRequest = request.getPrescriptionDiagnoses();
        if (diagnosisRequest == null || diagnosisRequest.isEmpty()) {
            throw new InvalidRequestException("At least one prescription diagnosis is required.");
        }
        for (int i = 0; i < diagnosisRequest.size(); i++) {
            CreatePrescriptionDiagnosisRequest diagnosis = diagnosisRequest.get(i);
            String prefix = "Prescription diagnosis " + (i + 1) + ": ";
            if (diagnosis == null) {
                throw new InvalidRequestException(prefix + "details are required.");
            }
            if (isBlank(diagnosis.getMedicineName())) {
                throw new InvalidRequestException(prefix + "medicine name is required.");
            }
            if (diagnosis.getStartDate() == null) {
                throw new InvalidRequestException(prefix + "start date is required.");
            }
        }
    }

    /**
     * Method to validate the Invoice Create Request.
     *
     * @param request: the Request to validate.
     */
    public static void validateInvoiceItems(CreateInvoiceRequest request) {
        if (Objects.isNull(request)) {
            throw new InvalidRequestException("Invoice request is required.");
        }
        if (Objects.isNull(request.getTotalPrice())) {
            throw new InvalidRequestException("Invoice total price is required.");
        }
        List<InvoiceItemsRequest> invoiceItems = request.getInvoiceItems();
        if (invoiceItems == null || invoiceItems.isEmpty()) {
            throw new InvalidRequestException("At least one invoice item is required.");
        }
        for (int i = 0; i < invoiceItems.size(); i++) {
            InvoiceItemsRequest item = invoiceItems.get(i);
            String prefix = "Invoice item " + (i + 1) + ": ";
            if (item == null) {
                throw new InvalidRequestException(prefix + "details are required.");
            }
            if (isBlank(item.getItemDescription())) {
                throw new InvalidRequestException(prefix + "description is required.");
            }
            if (isBlank(item.getBatchNumber())) {
                throw new InvalidRequestException(prefix + "batch number is required.");
            }
            if (Objects.isNull(item.getExpiryDate())) {
                throw new InvalidRequestException(prefix + "expiry date is required.");
            }
            if (Objects.isNull(item.getItemPrice())) {
                throw new InvalidRequestException(prefix + "item price is required.");
            }
            if (Objects.isNull(item.getItemTotalPrice())) {
                throw new InvalidRequestException(prefix + "line total is required.");
            }
            if (Objects.isNull(item.getMrp())) {
                throw new InvalidRequestException(prefix + "MRP is required.");
            }
        }
    }
}
