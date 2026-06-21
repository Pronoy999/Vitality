package com.vitality.api.repositories;

import com.vitality.api.entities.PurchaseOrder;
import com.vitality.api.entities.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("select p from PurchaseOrder p where p.status = :status and p.isActive = true")
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    @Query("UPDATE PurchaseOrder p SET p.status = :status WHERE p.id = :purchaseOrderId")
    int updatePurchaseOrderStatus(Long purchaseOrderId, PurchaseOrderStatus status);
}
