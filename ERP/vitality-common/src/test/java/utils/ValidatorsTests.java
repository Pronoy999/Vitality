package utils;

import com.vitality.common.dtos.*;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.utils.Validators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validators Test Suite")
public class ValidatorsTests {

    @Nested
    @DisplayName("validatePrescriptionCreateRequest Tests")
    class PrescriptionValidationTests {

        private CreatePrescriptionRequest validRequest;
        private CreatePrescriptionDiagnosisRequest validDiagnosis;

        @BeforeEach
        void setUp() {
            validDiagnosis = new CreatePrescriptionDiagnosisRequest();
            validDiagnosis.setMedicineName("Aspirin");
            validDiagnosis.setStartDate(LocalDate.now());
            validDiagnosis.setDosage("500mg");

            validRequest = new CreatePrescriptionRequest();
            validRequest.setFirstName("John");
            validRequest.setLastName("Doe");
            validRequest.setPatientPhoneNumber("1234567890");
            validRequest.setCustomerPhoneNumber("9876543210");
            validRequest.setCustomerFirstName("Jane");
            validRequest.setCustomerLastName("Smith");
            validRequest.setPrescriptionDiagnoses(List.of(validDiagnosis));
        }

        @Test
        @DisplayName("Should validate a valid prescription request successfully")
        void testValidPrescriptionRequest() {
            assertDoesNotThrow(() -> Validators.validatePrescriptionCreateRequest(validRequest));
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void testNullRequest() {
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(null));
            assertEquals("Prescription request is required.", exception.getMessage());
        }


        @Test
        @DisplayName("Should throw exception when patient phone number is empty")
        void testEmptyPatientPhoneNumber() {
            validRequest.setPatientPhoneNumber("");
            validRequest.setCustomerPhoneNumber(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Patient or customer phone number is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when customer phone number is empty and patient phone number is empty")
        void testBothPhoneNumbersEmpty() {
            validRequest.setPatientPhoneNumber("");
            validRequest.setCustomerPhoneNumber("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Patient or customer phone number is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when customer phone number equals patient phone number")
        void testCustomerPhoneEqualPatientPhone() {
            validRequest.setCustomerPhoneNumber("1234567890");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Customer phone number cannot be the same as patient phone number.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when customer phone provided without name")
        void testCustomerPhoneWithoutName() {
            validRequest.setCustomerFirstName("");
            validRequest.setCustomerLastName("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Customer first name and last name is required when customer phone number is provided.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when customer phone provided without first name but with last name")
        void testCustomerPhoneWithoutFirstName() {
            validRequest.setCustomerFirstName("FirstName");
            validRequest.setCustomerLastName("Smith");
            assertDoesNotThrow(() -> Validators.validatePrescriptionCreateRequest(validRequest));
        }

        @Test
        @DisplayName("Should throw exception when customer phone provided without last name but with first name")
        void testCustomerPhoneWithoutLastName() {
            validRequest.setCustomerFirstName("Jane");
            validRequest.setCustomerLastName("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Customer first name and last name is required when customer phone number is provided.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when first name is null")
        void testNullFirstName() {
            validRequest.setFirstName(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Patient first name is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when first name is empty")
        void testEmptyFirstName() {
            validRequest.setFirstName("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Patient first name is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when last name is null")
        void testNullLastName() {
            validRequest.setLastName(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Patient last name is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when last name is empty")
        void testEmptyLastName() {
            validRequest.setLastName("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Patient last name is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when prescription diagnoses list is null")
        void testNullDiagnosesList() {
            validRequest.setPrescriptionDiagnoses(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("At least one prescription diagnosis is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when prescription diagnoses list is empty")
        void testEmptyDiagnosesList() {
            validRequest.setPrescriptionDiagnoses(new ArrayList<>());
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("At least one prescription diagnosis is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when diagnosis is null in list")
        void testNullDiagnosisInList() {
            validRequest.setPrescriptionDiagnoses(Collections.emptyList());
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("At least one prescription diagnosis is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when medicine name is null")
        void testNullMedicineName() {
            validDiagnosis.setMedicineName(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Prescription diagnosis 1: medicine name is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when medicine name is empty")
        void testEmptyMedicineName() {
            validDiagnosis.setMedicineName("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Prescription diagnosis 1: medicine name is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when start date is null")
        void testNullStartDate() {
            validDiagnosis.setStartDate(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Prescription diagnosis 1: start date is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate when multiple diagnoses are present")
        void testMultipleDiagnoses() {
            CreatePrescriptionDiagnosisRequest diagnosis2 = new CreatePrescriptionDiagnosisRequest();
            diagnosis2.setMedicineName("Paracetamol");
            diagnosis2.setStartDate(LocalDate.now());

            validRequest.setPrescriptionDiagnoses(List.of(validDiagnosis, diagnosis2));
            assertDoesNotThrow(() -> Validators.validatePrescriptionCreateRequest(validRequest));
        }

        @Test
        @DisplayName("Should throw exception when second diagnosis is invalid")
        void testInvalidSecondDiagnosis() {
            CreatePrescriptionDiagnosisRequest diagnosis2 = new CreatePrescriptionDiagnosisRequest();
            diagnosis2.setStartDate(LocalDate.now());
            // Missing medicine name in second diagnosis

            validRequest.setPrescriptionDiagnoses(List.of(validDiagnosis, diagnosis2));
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validatePrescriptionCreateRequest(validRequest));
            assertEquals("Prescription diagnosis 2: medicine name is required.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateInvoiceItems Tests")
    class InvoiceValidationTests {

        private CreateInvoiceRequest validRequest;
        private InvoiceItemsRequest validItem;

        @BeforeEach
        void setUp() {
            validItem = new InvoiceItemsRequest();
            validItem.setItemDescription("Medicine A");
            validItem.setBatchNumber("BATCH001");
            validItem.setExpiryDate(LocalDate.now().plusMonths(6));
            validItem.setItemPrice(new BigDecimal("100.00"));
            validItem.setItemTotalPrice(new BigDecimal("1000.00"));
            validItem.setMrp(new BigDecimal("120.00"));

            validRequest = new CreateInvoiceRequest();
            validRequest.setTotalPrice(new BigDecimal("1000.00"));
            validRequest.setInvoiceItems(List.of(validItem));
        }

        @Test
        @DisplayName("Should validate a valid invoice request successfully")
        void testValidInvoiceRequest() {
            assertDoesNotThrow(() -> Validators.validateInvoiceItems(validRequest));
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void testNullRequest() {
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(null));
            assertEquals("Invoice request is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when total price is null")
        void testNullTotalPrice() {
            validRequest.setTotalPrice(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice total price is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when invoice items list is null")
        void testNullInvoiceItems() {
            validRequest.setInvoiceItems(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("At least one invoice item is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when invoice items list is empty")
        void testEmptyInvoiceItems() {
            validRequest.setInvoiceItems(new ArrayList<>());
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("At least one invoice item is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when item is null in list")
        void testNullItemInList() {
            validRequest.setInvoiceItems(Collections.emptyList());
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("At least one invoice item is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when item description is null")
        void testNullItemDescription() {
            validItem.setItemDescription(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: description is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when item description is empty")
        void testEmptyItemDescription() {
            validItem.setItemDescription("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: description is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when batch number is null")
        void testNullBatchNumber() {
            validItem.setBatchNumber(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: batch number is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when batch number is empty")
        void testEmptyBatchNumber() {
            validItem.setBatchNumber("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: batch number is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when expiry date is null")
        void testNullExpiryDate() {
            validItem.setExpiryDate(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: expiry date is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when item price is null")
        void testNullItemPrice() {
            validItem.setItemPrice(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: item price is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when item total price is null")
        void testNullItemTotalPrice() {
            validItem.setItemTotalPrice(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: line total is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when MRP is null")
        void testNullMrp() {
            validItem.setMrp(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 1: MRP is required.", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate when multiple items are present")
        void testMultipleInvoiceItems() {
            InvoiceItemsRequest item2 = new InvoiceItemsRequest();
            item2.setItemDescription("Medicine B");
            item2.setBatchNumber("BATCH002");
            item2.setExpiryDate(LocalDate.now().plusMonths(12));
            item2.setItemPrice(new BigDecimal("200.00"));
            item2.setItemTotalPrice(new BigDecimal("2000.00"));
            item2.setMrp(new BigDecimal("250.00"));

            validRequest.setInvoiceItems(List.of(validItem, item2));
            assertDoesNotThrow(() -> Validators.validateInvoiceItems(validRequest));
        }

        @Test
        @DisplayName("Should throw exception when second item is invalid")
        void testInvalidSecondItem() {
            InvoiceItemsRequest item2 = new InvoiceItemsRequest();
            item2.setItemDescription("Medicine B");
            item2.setBatchNumber("BATCH002");
            item2.setExpiryDate(LocalDate.now().plusMonths(12));
            item2.setItemPrice(new BigDecimal("200.00"));
            item2.setItemTotalPrice(new BigDecimal("2000.00"));
            // Missing MRP in second item

            validRequest.setInvoiceItems(List.of(validItem, item2));
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateInvoiceItems(validRequest));
            assertEquals("Invoice item 2: MRP is required.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateOrderRequest Tests")
    class OrderValidationTests {

        private CreateOrderRequest validRequest;
        private OrderRequestItems validItem;

        @BeforeEach
        void setUp() {
            validItem = new OrderRequestItems();
            validItem.setInventoryId(1L);
            validItem.setQuantity(new BigInteger("10"));
            validItem.setMarkupPercentage(new BigDecimal("10.00"));

            validRequest = new CreateOrderRequest();
            validRequest.setPatientFirstName("John");
            validRequest.setPatientLastName("Doe");
            validRequest.setOrderRequestItems(List.of(validItem));
        }

        @Test
        @DisplayName("Should validate a valid order request successfully")
        void testValidOrderRequest() {
            assertDoesNotThrow(() -> Validators.validateOrderRequest(validRequest));
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void testNullRequest() {
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateOrderRequest(null));
            assertEquals("Order request cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when both first and last names are empty")
        void testBothNamesEmpty() {
            validRequest.setPatientFirstName("");
            validRequest.setPatientLastName("");
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateOrderRequest(validRequest));
            assertEquals("Patient first name or last name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when both first and last names are null")
        void testBothNamesNull() {
            validRequest.setPatientFirstName(null);
            validRequest.setPatientLastName(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateOrderRequest(validRequest));
            assertEquals("Patient first name or last name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate when only first name is provided")
        void testOnlyFirstNameProvided() {
            validRequest.setPatientFirstName("John");
            validRequest.setPatientLastName("");
            assertDoesNotThrow(() -> Validators.validateOrderRequest(validRequest));
        }

        @Test
        @DisplayName("Should validate when only last name is provided")
        void testOnlyLastNameProvided() {
            validRequest.setPatientFirstName("");
            validRequest.setPatientLastName("Doe");
            assertDoesNotThrow(() -> Validators.validateOrderRequest(validRequest));
        }

        @Test
        @DisplayName("Should throw exception when order items list is null")
        void testNullOrderItems() {
            validRequest.setOrderRequestItems(null);
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateOrderRequest(validRequest));
            assertEquals("Order items cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when order items list is empty")
        void testEmptyOrderItems() {
            validRequest.setOrderRequestItems(new ArrayList<>());
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> Validators.validateOrderRequest(validRequest));
            assertEquals("Order items cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate when multiple order items are present")
        void testMultipleOrderItems() {
            OrderRequestItems item2 = new OrderRequestItems();
            item2.setInventoryId(2L);
            item2.setQuantity(new BigInteger("5"));
            item2.setMarkupPercentage(new BigDecimal("15.00"));

            validRequest.setOrderRequestItems(List.of(validItem, item2));
            assertDoesNotThrow(() -> Validators.validateOrderRequest(validRequest));
        }
    }
}
