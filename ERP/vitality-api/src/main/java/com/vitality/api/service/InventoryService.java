package com.vitality.api.service;

import com.vitality.api.entities.Inventory;
import com.vitality.api.entities.Invoice;
import com.vitality.api.entities.InvoiceItem;
import com.vitality.api.mappers.ResponseMappers;
import com.vitality.api.repositories.InventoryRepository;
import com.vitality.common.dtos.GetInventoryRequest;
import com.vitality.common.dtos.GetInventoryResponse;
import com.vitality.common.utils.CommonUtils;
import com.vitality.common.utils.FinanceUtils;
import com.vitality.common.utils.ResponseGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public ResponseEntity<?> searchInventory(GetInventoryRequest request) {
        try {
            List<Inventory> inventoryList;
            if (request != null) {
                Inventory inventories = inventoryRepository.findByItemDescAndBatchNumberAndExpiryDate(request.getItemDesc(), request.getBatchNumber(), request.getExpiryDate());
                inventoryList = Collections.singletonList(inventories);
            } else {
                inventoryList = getEntireInventory();
            }
            List<GetInventoryResponse> responses = ResponseMappers.mapToGetInventoryResponse(inventoryList);
            log.info("Returning Inventory with items: {}", responses.size());
            return ResponseGenerator.generateSuccessResponse(responses, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching inventory: ", e);
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch inventory. Please try again later.");
        }
    }

    public List<Inventory> getEntireInventory() {
        try {
            return inventoryRepository.findAll();
        } catch (Exception e) {
            log.error("Error fetching inventory: ", e);
            throw new RuntimeException("Failed to fetch inventory. Please try again later.");
        }
    }

    /**
     * Method to update the Inventory with new stock and update qty for existing stock.
     *
     * @param invoice: The Invoice which is full-filled.
     */
    protected void updateInventory(Invoice invoice) {
        List<Inventory> existingInventory = getEntireInventory();
        Map<String, Inventory> inventoryMap = existingInventory.stream().collect(
                Collectors.toMap(inv -> CommonUtils.getUniqueItemKey(inv.getItemDescription(), inv.getBatchNumber(), inv.getExpiryDate()), inv -> inv));
        List<Inventory> toSave = new ArrayList<>();
        List<InvoiceItem> invoiceItems = invoice.getInvoiceItems();
        invoiceItems.forEach(invoiceItem -> {
            String key = CommonUtils.getUniqueItemKey(invoiceItem.getItemDesc(), invoiceItem.getBatchNumber(), invoiceItem.getExpiryDate());
            Inventory inventory = inventoryMap.get(key);
            if (inventory != null) {
                BigInteger newQty = inventory.getQuantityAvailable().add(invoiceItem.getReceivedItemQty()).add(invoiceItem.getFreeItemQty());
                inventory.setQuantityAvailable(newQty);
                inventory.setMrp(invoiceItem.getMrp());
                inventory.setManufacturingDate(invoiceItem.getManufacturedDate());
                inventory.setTaxPercentage(FinanceUtils.normalizePercentage(invoiceItem.getTaxPercentage()));
                inventory.setInvoice(invoice);
                inventory.setUpdatedTimestamp(LocalDateTime.now());
            } else {
                inventory = new Inventory();
                inventory.setItemDescription(invoiceItem.getItemDesc());
                inventory.setBatchNumber(invoiceItem.getBatchNumber());
                inventory.setExpiryDate(invoiceItem.getExpiryDate());
                inventory.setManufacturingDate(invoiceItem.getManufacturedDate());
                inventory.setQuantityAvailable(invoiceItem.getReceivedItemQty().add(invoiceItem.getFreeItemQty()));
                inventory.setMrp(invoiceItem.getMrp());
                inventory.setPurchasePrice(FinanceUtils.getItemPriceWithTax(invoiceItem.getItemPrice(), invoiceItem.getTaxPercentage(), null));
                inventory.setTaxPercentage(FinanceUtils.normalizePercentage(invoiceItem.getTaxPercentage()));
                inventory.setCreatedTimestamp(LocalDateTime.now());
                inventory.setSupplier(invoice.getSupplier());
            }
            toSave.add(inventory);
        });
        inventoryRepository.saveAll(toSave);
        log.info("Inventory updated successfully for invoice id: {} with items: {}", invoice.getId(), toSave.size());
    }

    protected List<Inventory> getItemsById(List<Long> ids) {
        return inventoryRepository.findAllById(ids);
    }

    /**
     * Method to reduce the Item quantity from Inventory when an order is full-filled.
     * It will check if the quantity is sufficient before reducing and throw an exception if the quantity goes negative.
     *
     * @param itemsSold: the Map of Inventory Id and quantity sold for that item.
     */
    protected void reduceInventoryQuantity(Map<Long, BigInteger> itemsSold) {
        List<Inventory> inventoryList = getItemsById(new ArrayList<>(itemsSold.keySet()));
        inventoryList.forEach(inventory -> {
            BigInteger soldQty = itemsSold.get(inventory.getId());
            if (soldQty != null) {
                BigInteger newQty = inventory.getQuantityAvailable().subtract(soldQty);
                if (newQty.compareTo(BigInteger.ZERO) < 0) {
                    log.error("Inventory quantity for item {} can't be negative", inventory.getItemDescription());
                    throw new RuntimeException("Inventory quantity for item " + inventory.getItemDescription() + " can't be negative");
                }
                inventory.setQuantityAvailable(newQty);
            }
        });
        inventoryRepository.saveAll(inventoryList);
    }
}
