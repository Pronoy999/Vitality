package com.vitality.common.utils;

import com.vitality.common.dtos.*;
import com.vitality.common.exceptions.InvalidRequestException;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;


public class Validators {
    /**
     * Method to validate the create Prescription Request.
     * It throws {@link InvalidRequestException} when any of the required field is missing or invalid in the request.
     *
     * @param request: the request to be validated.
     */
    public static void validatePrescriptionCreateRequest(CreatePrescriptionRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Prescription request is required.");
        }
        if (((request.getPatientPhoneNumber() == null || !StringUtils.hasLength(request.getPatientPhoneNumber()))) ||
                (request.getCustomerPhoneNumber() == null || !StringUtils.hasLength(request.getCustomerPhoneNumber()))) {
            throw new InvalidRequestException("Patient or customer phone number is required.");
        }
        if (StringUtils.hasLength(request.getCustomerPhoneNumber()) && request.getCustomerPhoneNumber().equals(request.getPatientPhoneNumber())) {
            throw new InvalidRequestException("Customer phone number cannot be the same as patient phone number.");
        }
        if (StringUtils.hasLength(request.getCustomerPhoneNumber()) &&
                (!StringUtils.hasLength(request.getCustomerFirstName())) && !StringUtils.hasLength(request.getCustomerLastName())) {
            throw new InvalidRequestException("Customer first name or last name is required when customer phone number is provided.");
        }
        if (request.getFirstName() == null || !StringUtils.hasLength(request.getFirstName())) {
            throw new InvalidRequestException("Patient first name is required.");
        }
        if (request.getLastName() == null || !StringUtils.hasLength(request.getLastName())) {
            throw new InvalidRequestException("Patient last name is required.");
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
            if (!StringUtils.hasLength(diagnosis.getMedicineName())) {
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
            if (!StringUtils.hasLength(item.getItemDescription())) {
                throw new InvalidRequestException(prefix + "description is required.");
            }
            if (!StringUtils.hasLength(item.getBatchNumber())) {
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

    public static void validateOrderRequest(CreateOrderRequest request) {
        if (Objects.isNull(request)) {
            throw new InvalidRequestException("Order request cannot be null");
        }
        if (!StringUtils.hasLength(request.getPatientFirstName()) && !StringUtils.hasLength(request.getPatientLastName())) {
            throw new InvalidRequestException("Patient first name or last name cannot be empty");
        }
        if (Objects.isNull(request.getOrderRequestItems()) || request.getOrderRequestItems().isEmpty()) {
            throw new InvalidRequestException("Order items cannot be empty");
        }
    }
}
