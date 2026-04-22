package com.vitality.api.repositories;

import com.vitality.api.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT i from Inventory i WHERE i.itemDescription = :itemDesc AND i.batchNumber = :batchNumber AND i.expiryDate = :expiryDate")
    Inventory findByItemDescAndBatchNumberAndExpiryDate(String itemDesc, String batchNumber, LocalDate expiryDate);
}
