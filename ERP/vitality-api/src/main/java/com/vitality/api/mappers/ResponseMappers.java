package com.vitality.api.mappers;

import com.vitality.api.entities.Invoice;
import com.vitality.common.dtos.InvoiceItemResponse;
import com.vitality.common.dtos.InvoiceResponse;

import java.util.List;

public class ResponseMappers {
    /**
     * Maps an Invoice entity to an InvoiceResponse DTO, including safe extraction of supplier details and invoice items.
     *
     * @param invoice: The Invoice entity to be mapped to a response DTO.
     * @return An InvoiceResponse DTO containing the mapped data from the Invoice entity.
     */
    public static InvoiceResponse mapToInvoiceResponse(Invoice invoice) {

        InvoiceResponse response = new InvoiceResponse();

        response.setId(invoice.getId());
        response.setInvoiceId(invoice.getInvoiceId());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setReceivedDate(invoice.getReceivedDate());
        response.setStatus(invoice.getStatus().name());

        response.setItemTotalPrice(invoice.getItemTotalPrice());
        response.setTotalDiscount(invoice.getTotalDiscount());
        response.setLogisticAmount(invoice.getLogisticAmount());
        response.setInsuranceAmount(invoice.getInsuranceAmount());
        response.setRoundOffAmount(invoice.getRoundOffAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setTotalPrice(invoice.getTotalPrice());

        response.setIsActive(invoice.getIsActive());
        response.setCreatedTimestamp(invoice.getCreatedTimestamp());
        response.setUpdatedTimestamp(invoice.getUpdatedTimestamp());

        // Supplier (safe extraction)
        if (invoice.getSupplier() != null) {
            response.setSupplierId(invoice.getSupplier().getId());
            response.setSupplierName(invoice.getSupplier().getSupplierName());
        }

        // Purchase Order
        if (invoice.getPurchaseOrder() != null) {
            response.setPurchaseOrderId(invoice.getPurchaseOrder().getId());
        }

        // Items
        List<InvoiceItemResponse> items = invoice.getInvoiceItems()
                .stream()
                .map(item -> {
                    InvoiceItemResponse dto = new InvoiceItemResponse();
                    dto.setId(item.getId());
                    dto.setItemDesc(item.getItemDesc());
                    dto.setReceivedItemQty(item.getReceivedItemQty());
                    dto.setDamagedItemQty(item.getDamagedItemQty());
                    dto.setFreeItemQty(item.getFreeItemQty());
                    dto.setItemPrice(item.getItemPrice());
                    dto.setHsnCode(item.getHsnCode());
                    dto.setExpiryDate(item.getExpiryDate());
                    dto.setManufacturedDate(item.getManufacturedDate());
                    dto.setBatchNumber(item.getBatchNumber());
                    dto.setTaxPercentage(item.getTaxPercentage());
                    dto.setItemTotalPrice(item.getItemTotalPrice());
                    dto.setMrp(item.getMrp());
                    return dto;
                })
                .toList();

        response.setItems(items);

        return response;
    }
}