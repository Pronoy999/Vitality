package com.vitality.api.service;

import com.vitality.api.entities.Invoice;
import com.vitality.api.entities.InvoiceItem;
import com.vitality.api.entities.InvoiceStatus;
import com.vitality.api.entities.Supplier;
import com.vitality.api.mappers.ResponseMappers;
import com.vitality.api.repositories.InvoiceRepository;
import com.vitality.common.dtos.*;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.Validators;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Method to create the invoice.
     *
     * @param request: The invoice creation request.
     */
    @Transactional
    public ResponseEntity<?> createInvoice(@NotNull CreateInvoiceRequest request) {
        Validators.validateInvoiceItems(request);

        if (request.getSupplierId() == null && request.getSupplierName() == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Either supplier id or supplier name is required to create an invoice.");
        }
        Supplier supplier = getSupplier(request.getSupplierName(), request.getSupplierId());
        Invoice invoice = getInvoiceDetails(request, supplier);
        List<InvoiceItem> invoiceItems = createInvoiceItem(request.getInvoiceItems(), invoice);
        if (invoiceItems.isEmpty()) {
            throw new InvalidRequestException("Invoice Items can't be empty");
        }
        invoice.setInvoiceItems(invoiceItems);
        invoice = invoiceRepository.save(invoice);
        log.info("Invoice created successfully with id: {} with: {} items", invoice.getId(), invoiceItems.size());
        if (invoice.getStatus().equals(InvoiceStatus.INVOICE_DELIVERED)) {
            log.info("Invoice Items are delivered, hence Updating Inventory.");
            Invoice finalInvoice = invoice;
            executorService.submit(() -> inventoryService.updateInventory(finalInvoice));
        }
        CreateInvoiceResponse response = new CreateInvoiceResponse(invoice.getId());
        return ResponseGenerator.generateSuccessResponse(response, HttpStatus.CREATED);
    }

    /**
     * Method to fetch all the invoices.
     *
     * @return the list of {@link Invoice}
     */
    public ResponseEntity<?> getAllInvoices() {
        try {
            List<Invoice> invoices = invoiceRepository.findAllInvoices();
            List<InvoiceResponse> response = invoices.stream()
                    .map(ResponseMappers::mapToInvoiceResponse)
                    .toList();
            return ResponseGenerator.generateSuccessResponse(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching invoices: ", e);
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch invoices. Please try again later.");
        }
    }

    private Invoice getInvoiceDetails(@NotNull CreateInvoiceRequest request, Supplier supplier) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(request.getInvoiceNumber());
        invoice.setInvoiceDate(request.getInvoiceDate());
        if (request.isAreItemsDelivered()) {
            invoice.setStatus(InvoiceStatus.INVOICE_DELIVERED);
        }
        invoice.setSupplier(supplier);
        invoice.setReceivedDate(request.getReceivedDate());
        invoice.setItemTotalPrice(request.getItemTotalPrice());
        invoice.setTotalDiscount(request.getDiscountAmount());
        invoice.setLogisticAmount(request.getLogisticsAmount());
        invoice.setInsuranceAmount(request.getInsuranceAmount());
        invoice.setRoundOffAmount(request.getRoundOffAmount());
        invoice.setTaxAmount(request.getTaxAmount());
        invoice.setTotalPrice(request.getTotalPrice());
        invoice.setIsActive(true);
        return invoice;
    }

    private Supplier getSupplier(@NotNull String supplierName, Long supplierId) {
        if (supplierId != null) {
            return supplierService.getSupplierById(supplierId);
        } else {
            CreateSupplierRequest createSupplierRequest = new CreateSupplierRequest(supplierName);
            return supplierService.doCreateSupplier(createSupplierRequest);
        }
    }

    private List<InvoiceItem> createInvoiceItem(List<InvoiceItemsRequest> itemRequests, Invoice invoice) {
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        itemRequests.forEach(item -> {
            if (item.getItemDescription() != null) {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setInvoice(invoice);
                invoiceItem.setItemDesc(item.getItemDescription());
                invoiceItem.setReceivedItemQty(item.getReceivedQuantity());
                invoiceItem.setDamagedItemQty(item.getDamagedQuantity());
                invoiceItem.setFreeItemQty(item.getFreeQuantity());
                invoiceItem.setItemPrice(item.getItemPrice());
                invoiceItem.setHsnCode(item.getHsnCode());
                invoiceItem.setExpiryDate(item.getExpiryDate());
                invoiceItem.setManufacturedDate(item.getManufacturedDate());
                invoiceItem.setBatchNumber(item.getBatchNumber());
                invoiceItem.setTaxPercentage(item.getTaxPercentage());
                invoiceItem.setItemTotalPrice(item.getItemTotalPrice());
                invoiceItem.setMrp(item.getMrp());
                invoiceItem.setIsActive(true);
                invoiceItems.add(invoiceItem);
            }
        });
        return invoiceItems;
    }
}
