package com.vitality.api.repositories;

import com.vitality.api.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    @Query("SELECT s FROM Supplier s WHERE s.supplierName LIKE :supplierName and s.isActive = true")
    List<Supplier> searchBySupplierName(String supplierName);
}
