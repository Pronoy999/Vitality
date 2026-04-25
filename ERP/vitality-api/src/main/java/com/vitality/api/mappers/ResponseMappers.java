package com.vitality.api.mappers;

import com.vitality.api.entities.*;
import com.vitality.common.dtos.*;
import org.aspectj.weaver.ast.Or;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static List<GetInventoryResponse> mapToGetInventoryResponse(List<Inventory> inventories) {
        List<GetInventoryResponse> responses = new ArrayList<>();
        inventories.forEach(inventory -> {
            GetInventoryResponse response = new GetInventoryResponse();
            response.setInventoryId(inventory.getId());
            response.setItemDescription(inventory.getItemDescription());
            response.setQuantityAvailable(inventory.getQuantityAvailable());
            response.setQuantityReserved(inventory.getQuantityReserved());
            response.setBatchNumber(inventory.getBatchNumber());
            response.setManufacturingDate(inventory.getManufacturingDate());
            response.setExpiryDate(inventory.getExpiryDate());
            response.setPurchasePrice(inventory.getPurchasePrice());
            response.setTaxPercentage(inventory.getTaxPercentage());
            response.setMrp(inventory.getMrp());
            response.setSupplierName(inventory.getSupplier().getSupplierName());
            responses.add(response);
        });
        return responses;
    }

    public static OrderInvoice mapToOrderInvoice(List<OrderInvoiceView> orderInvoiceView) {
        if (orderInvoiceView.isEmpty()) {
            return null;
        }
        OrderInvoice invoice = new OrderInvoice();
        OrderInvoiceView first = orderInvoiceView.get(0);
        invoice.setOrderId(first.getOrderId());
        invoice.setOrderDate(first.getOrderDate());
        invoice.setPatientName(first.getFirstName() + " " + first.getLastName());
        invoice.setTotalItemPrice(first.getTotalItemPrice());
        invoice.setTotalDiscount(first.getTotalDiscount());
        invoice.setTotalTaxAmount(first.getTotalTaxAmount());
        invoice.setPlatformFee(first.getPlatformFee());
        invoice.setDeliveryFee(first.getDeliveryFee());
        invoice.setRoundOffAmount(first.getRoundOffAmount());
        invoice.setTotalPrice(first.getTotalPrice());

        List<OrderItemInvoice> items = new ArrayList<>();
        orderInvoiceView.forEach(item -> {
            OrderItemInvoice itemInvoice = new OrderItemInvoice();
            itemInvoice.setItemDescription(item.getItemDescription());
            itemInvoice.setQuantity(item.getQuantity());
            itemInvoice.setItemPrice(item.getItemPrice());
            itemInvoice.setItemDiscount(item.getItemDiscount());
            itemInvoice.setCgstAmount(item.getCgstAmount());
            itemInvoice.setSgstAmount(item.getSgstAmount());
            itemInvoice.setItemTotalPrice(item.getItemTotalPrice());
            items.add(itemInvoice);
        });
        invoice.setItems(items);
        return invoice;
    }
}