package com.vitality.api.repositories;

import com.vitality.api.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.invoiceItems WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(Long id);

    @Query("""
                SELECT DISTINCT i
                FROM Invoice i
                LEFT JOIN FETCH i.supplier
                LEFT JOIN FETCH i.invoiceItems
            """)
    List<Invoice> findAllInvoices();
}
