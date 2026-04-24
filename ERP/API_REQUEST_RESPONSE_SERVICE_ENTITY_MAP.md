# Vitality ERP API Knowledge Map

## Purpose
This document maps each backend request path to:
- controller method
- service method(s)
- request/response DTOs or payload shape
- persisted entities/repositories touched

Use this as the first reference before changing APIs, debugging, or adding features.

## Base API And Conventions
- Base path: `/api/v1/vitality`
- JWT header key for protected APIs: `token`
- Success response format: raw DTO/object returned with HTTP status (no wrapper)
- Error response format: `ErrorResponse { errorCode, errorMessage }`

## Endpoint Map

### 1) Health Check
- Method + Path: `GET /api/v1/vitality/health`
- Controller: `HealthCheckController.healthCheck`
- Service: none
- Request body: none
- Success response:
  - HTTP `200`
  - body: string message (`"Vitality API is up and running"`)
- Entities touched: none

### 2) User Create/Login (Google OAuth)
- Method + Path: `POST /api/v1/vitality/user`
- Controller: `UsersController.createOrLoginUser`
- Service flow:
  - `UserService.createOrLoginUser`
  - `SecurityUtils.verifyGoogleOAuthToken`
  - lookup/create user + credentials
  - `SecurityUtils.createJwt`
- Request DTO: `CreateLoginUserRequest`
  - `email`, `firstName`, `lastName`, `googleToken`, `phoneNumber`, `dateOfBirth`, `gender`
- Success response:
  - HTTP `200`
  - `LoginUserResponse { jwtToken, userId }`
- Failure response:
  - HTTP `400` when both email and googleToken missing
  - HTTP `401` on invalid Google token
  - HTTP `500` unexpected errors
- Entities + repos:
  - `User` via `UserRepository`
  - `Credentials` via `CredentialsRepository`

### 3) Patient Create/Update (Protected)
- Method + Path: `POST /api/v1/vitality/patient`
- Controller: `PatientController.createPatient`
- Auth: validates `token` header using `SecurityUtils.validateRequest`
- Service flow:
  - `PatientService.createPatient`
- Request DTO: `CreatePatientRequest`
  - `firstName`, `lastName`, `phoneNumber`, `email`, `age`, `height`, `weight`, `gender`, `bloodPressure`, `ailmentHistory`, `hasHealthInsurance`, `additionalDiagnosis`, `medicinesConsumed`, `additionalServicesRequired`, `healthParameters`, `abhaId`
- Current implementation note:
  - `PatientService.createPatient(...)` currently returns `null` (not implemented yet).
- Expected entities in intended flow (based on helper methods):
  - `Patient` via `PatientRepository`
  - `User`/`Credentials` through `UserService.searchOrCreatePatientUser`

### 4) Prescription Create (Manual Full Payload, Protected)
- Method + Path: `POST /api/v1/vitality/prescription`
- Controller: `PrescriptionController.createPrescription`
- Auth: validates `token` header
- Service flow:
  - `PrescriptionService.createPrescription`
  - `PrescriptionService.persistPrescription(requirePatientName=true, requireDiagnosisRows=true)`
  - `PatientService.searchPatient` and optional `PatientService.doCreatePatient`
  - create diagnosis rows
- Request DTO: `CreatePrescriptionRequest` (extends `CreatePatientRequest`)
  - inherited patient fields + `prescriptionDate`, `referredByDoctor`, `prescriptionImageUrl`, `diagnosis`, `prescriptionDiagnoses[]`
  - each diagnosis item: `CreatePrescriptionDiagnosisRequest { diagnosis, medicineName, dosage, unit, unitMeasure, startDate, endDate, frequency }`
- Success response:
  - HTTP `201`
  - `CreatePrescriptionResponse { prescriptionId }`
- Failure response:
  - HTTP `400` for missing names / invalid diagnosis rows
  - HTTP `500` for unexpected failures
- Entities + repos:
  - `Prescription` via `PrescriptionRepository`
  - `PrescriptionDiagnosis` via `PrescriptionDiagnosisRepository`
  - `Patient` via `PatientRepository`
  - indirect `User`/`Credentials` when auto-creating patient

### 5) Prescription Upload (AI Parse Job, Protected)
- Method + Path: `POST /api/v1/vitality/prescription/upload`
- Controller: `PrescriptionController.upload`
- Auth: validates `token` header
- Request type: multipart form-data
  - key: `files` (list of image files)
- Service flow:
  - controller stores files to temp dir
  - creates in-memory job (`jobs` map)
  - async task calls `GeminiApiService.parsePrescription`
- Success response:
  - HTTP `200`
  - `{ "job_id": "<uuid>" }`
- Failure response:
  - HTTP `400` if files missing/empty
  - HTTP `500` if Gemini not configured or upload fails
- Entities touched:
  - none (in-memory jobs + temp filesystem only)

### 6) Prescription Job Status (Protected)
- Method + Path: `GET /api/v1/vitality/prescription/status/{jobId}`
- Controller: `PrescriptionController.status`
- Auth: validates `token` header
- Request params:
  - path variable: `jobId`
- Success response:
  - HTTP `200`
  - `{ status, step, data, error }`
  - `data` is `ParsedPrescriptionData` when ready
- Failure response:
  - HTTP `404` if job not found
- Entities touched: none

### 7) Prescription Confirm Parsed Data (Protected)
- Method + Path: `POST /api/v1/vitality/prescription/confirm/{jobId}`
- Controller: `PrescriptionController.confirm`
- Auth: validates `token` header
- Request DTO: `PrescriptionDataRequest { data: ParsedPrescriptionData }`
- Service flow:
  - validates job presence and data
  - `PrescriptionService.createPrescriptionFromParsedData`
  - converts parsed data to `CreatePrescriptionRequest`
  - persists prescription + diagnosis rows
- Success response:
  - HTTP `201`
  - python-compatible map: `{ "prescription_id": <id> }`
- Failure response:
  - HTTP `404` job missing
  - HTTP `400` missing data
  - HTTP `500` persistence errors
- Entities + repos:
  - `Prescription`, `PrescriptionDiagnosis`, `Patient` (+ optional `User`/`Credentials` indirectly)

### 8) Prescription Manual Save From Parsed Data (Protected)
- Method + Path: `POST /api/v1/vitality/prescription/manual`
- Controller: `PrescriptionController.saveManualPrescription`
- Auth: validates `token` header
- Request DTO: `PrescriptionDataRequest { data: ParsedPrescriptionData }`
- Service flow:
  - `PrescriptionService.createPrescriptionFromParsedData`
- Success response:
  - HTTP `201`
  - `{ "prescription_id": <id> }`
- Failure response:
  - HTTP `400` missing data
  - HTTP `500` persistence errors
- Entities + repos:
  - `Prescription`, `PrescriptionDiagnosis`, `Patient` (+ optional `User`/`Credentials` indirectly)

### 9) Invoice Create (Protected)
- Method + Path: `POST /api/v1/vitality/invoice`
- Controller: `InvoiceController.createInvoice`
- Auth: validates `token` header
- Service flow:
  - `InvoiceService.createInvoice`
  - supplier resolution (`SupplierService.getSupplierById` OR `SupplierService.doCreateSupplier`)
  - invoice persistence
  - invoice items persistence
- Request DTO: `CreateInvoiceRequest`
  - `purchaseOrderId`, `invoiceNumber`, `supplierName`, `supplierId`, `invoiceDate`, `receivedDate`, `areItemsDelivered`, `itemTotalPrice`, `discountAmount`, `logisticsAmount`, `insuranceAmount`, `roundOffAmount`, `taxAmount`, `totalPrice`, `invoiceItems[]`
  - items: `InvoiceItemsRequest { itemDescription, receivedQuantity, damagedQuantity, freeQuantity, itemPrice, hsnCode, expiryDate, manufacturedDate, batchNumber, taxPercentage, itemTotalPrice, mrp }`
- Success response:
  - HTTP `201`
  - `CreateInvoiceResponse { invoiceId }`
- Failure response:
  - HTTP `400` if supplier info missing
  - HTTP `500` for creation errors
- Entities + repos:
  - `Invoice` via `InvoiceRepository`
  - `InvoiceItem` via `InvoiceItemRepository`
  - `Supplier` via `SupplierRepository`/`SupplierService`

### 10) Invoice Upload (AI Parse Job, Protected)
- Method + Path: `POST /api/v1/vitality/invoice/upload`
- Controller: `InvoiceController.upload`
- Auth: validates `token` header
- Request: multipart form-data with `files`
- Service flow:
  - stores invoice images under temp `invoice_uploads/{jobId}`
  - creates in-memory async job
  - async task calls `GeminiApiService.parseInvoice`
- Success response:
  - HTTP `200`
  - `{ "job_id": "<uuid>" }`
- Failure response:
  - HTTP `400` if no invoice image is provided
  - HTTP `500` if Gemini is not configured or upload storage fails

### 11) Invoice Job Status (Protected)
- Method + Path: `GET /api/v1/vitality/invoice/status/{jobId}`
- Controller: `InvoiceController.status`
- Auth: validates `token` header
- Request body: none
- Success response:
  - HTTP `200`
  - `{ status, step, data, error }`
  - `data` is `ParsedInvoiceData` when ready
- Failure response:
  - HTTP `404` if job is not found

### 12) Invoice List (Protected)
- Method + Path: `GET /api/v1/vitality/invoice`
- Controller: `InvoiceController.getInvoice`
- Auth: validates `token` header
- Service flow:
  - `InvoiceService.getAllInvoices`
  - `InvoiceRepository.findAllInvoices` (fetch supplier + items)
  - `ResponseMappers.mapToInvoiceResponse`
- Request body: none
- Success response:
  - HTTP `200`
  - `List<InvoiceResponse>`
  - includes flattened supplier fields and item list (`InvoiceItemResponse[]`)
- Failure response:
  - HTTP `500` on fetch errors
- Entities + repos:
  - `Invoice`, `Supplier`, `InvoiceItem` via `InvoiceRepository`

## Parsed Invoice Data Contract
Used in invoice upload/status flow. Fields mirror `CreateInvoiceRequest`, with invoice item fields mirrored in `ParsedInvoiceItemData`.

- `ParsedInvoiceData`
  - `purchaseOrderId`
  - `invoiceNumber`
  - `supplierName`
  - `supplierId`
  - `invoiceDate`
  - `receivedDate`
  - `areItemsDelivered`
  - `itemTotalPrice`
  - `discountAmount`
  - `logisticsAmount`
  - `insuranceAmount`
  - `roundOffAmount`
  - `taxAmount`
  - `totalPrice`
  - `invoiceItems[]`
- `ParsedInvoiceItemData`
  - `itemDescription`
  - `receivedQuantity`
  - `damagedQuantity`
  - `freeQuantity`
  - `itemPrice`
  - `hsnCode`
  - `expiryDate`
  - `manufacturedDate`
  - `batchNumber`
  - `taxPercentage`
  - `itemTotalPrice`
  - `mrp`

## Parsed Prescription Data Contract
Used in upload/confirm/manual flow.

- `ParsedPrescriptionData`
  - `patient_name`
  - `patient_age`
  - `doctor_name`
  - `date`
  - `patient_issue`
  - `diagnosis`
  - `health_metrics` (map)
  - `medicines[]`
- `ParsedMedicineData`
  - `name`, `dosage`, `quantity`

## Service-to-Entity Matrix

- `UserService`
  - writes/reads: `User`, `Credentials`
  - responsibilities: Google token verification, user lookup/create, JWT issue

- `PatientService`
  - writes/reads: `Patient`
  - indirect user linking via `UserService`
  - note: public `createPatient` currently not implemented

- `PrescriptionService`
  - writes: `Prescription`, `PrescriptionDiagnosis`
  - reads/creates: `Patient` via `PatientService`
  - maps parsed AI payload into prescription model

- `InvoiceService`
  - writes: `Invoice`, `InvoiceItem`
  - reads/creates supplier via `SupplierService`

- `SupplierService`
  - reads/writes: `Supplier`

- `GeminiApiService`
  - no DB entities
  - calls external Gemini API, returns `ParsedPrescriptionData`

## Entity Inventory (JPA)
Primary entities actively used by current API endpoints:
- `User` (`tbl_users`)
- `Credentials` (`tbl_credentials`)
- `Patient` (`tbl_patients`)
- `Prescription` (`tbl_prescription`)
- `PrescriptionDiagnosis` (`tbl_prescription_diagnosis`)
- `Supplier` (`tbl_supplier`)
- `Invoice` (`tbl_invoice`)
- `InvoiceItem` (`tbl_invoice_items`)

Other mapped entities present in codebase (not currently wired through public controllers):
- `Inventory`, `PurchaseOrder`, `PurchaseOrderItem`, `AilmentType`, `PatientAilments`, `MedicinesConsumed`, `Otp`, `Roles`

## Important Gaps / Risks To Remember
- `PatientService.createPatient(...)` returns `null`; patient endpoint behavior is incomplete.
- `Invoice.purchaseOrder` is marked `nullable = false`, but `InvoiceService` does not set it during create; this may fail at runtime depending on DB constraints.
- `SupplierService.getSupplierById` throws `orElseThrow(() -> null)` (unsafe null supplier on missing ID).
- `SupplierService.doCreateSupplier` sets `pocContact` twice (from `pocName` and `pocPhone`), so `pocName` is lost.
- `CreatePrescriptionRequest` has `@NotEmpty` on `prescriptionImageUrl` and diagnosis list, but parsed-data flow bypasses strict requirement via custom service flags.

## UI Request Mapping (Current Angular)
- `PatientInfoFormComponent` sends:
  - `POST http://122.166.244.91:8080/api/v1/vitality/patient`
  - payload currently contains `name` (single field), but backend expects `firstName` + `lastName`.
  - no `token` header is attached though backend endpoint is protected.

