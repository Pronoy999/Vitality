package com.vitality.api.service;

import com.vitality.api.entities.Invoice;
import com.vitality.api.entities.InvoiceItem;
import com.vitality.api.entities.Supplier;
import com.vitality.api.repositories.InvoiceItemRepository;
import com.vitality.api.repositories.InvoiceRepository;
import com.vitality.common.dtos.CreateInvoiceRequest;
import com.vitality.common.dtos.CreateInvoiceResponse;
import com.vitality.common.dtos.CreateSupplierRequest;
import com.vitality.common.dtos.InvoiceItemsRequest;
import com.vitality.common.utils.ResponseGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final SupplierService supplierService;
    private final InvoiceItemRepository invoiceItemRepository;

    /**
     * Method to create the invoice.
     *
     * @param request: The invoice creation request.
     */
    public ResponseEntity<?> createInvoice(@NotNull CreateInvoiceRequest request) {
        if (request.getSupplierId() == null && request.getSupplierName() == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Either supplier id or supplier name is required to create an invoice.");
        }
        try {
            Supplier supplier = getSupplier(request.getSupplierName(), request.getSupplierId());
            Invoice invoice = getInvoiceDetails(request, supplier);
            invoice = invoiceRepository.save(invoice);
            log.info("Invoice created successfully with id: {}", invoice.getId());
            List<InvoiceItem> invoiceItems = createInvoiceItem(request.getInvoiceItems(), invoice);
            invoiceItemRepository.saveAll(invoiceItems);
            log.info("Invoice items created successfully for: {} items", invoiceItems.size());
            CreateInvoiceResponse response = new CreateInvoiceResponse(invoice.getId());
            return ResponseGenerator.generateSuccessResponse(response, HttpStatus.CREATED);
            //TODO: Update Inventory.
        } catch (Exception e) {
            log.error("Error creating invoice: ", e);
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create invoice. Please try again later.");
        }
    }

    private Invoice getInvoiceDetails(@NotNull CreateInvoiceRequest request, Supplier supplier) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(request.getInvoiceNumber());
        invoice.setInvoiceDate(request.getInvoiceDate());
        if (request.isAreItemsDelivered()) {
            invoice.setStatus("DELIVERED");
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
            }
        });
        return invoiceItems;
    }
}
