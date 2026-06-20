package com.vitality.api.repositories;

import com.vitality.api.entities.Inventory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT i from Inventory i WHERE i.itemDescription = :itemDesc AND i.batchNumber = :batchNumber AND i.expiryDate = :expiryDate")
    Inventory findByItemDescAndBatchNumberAndExpiryDate(String itemDesc, String batchNumber, LocalDate expiryDate);

    @Query("SELECT i FROM Inventory i WHERE i.id IN :ids and i.isActive=true")
    List<Inventory> findAllById(@Param("ids") List<Long> ids);

    @Query("SELECT i from Inventory i where i.expiryDate<:expiringDateThreshold and i.isActive=true order by i.expiryDate asc")
    List<Inventory> findExpiringInventory(LocalDate expiringDateThreshold, Pageable pageable);
}
