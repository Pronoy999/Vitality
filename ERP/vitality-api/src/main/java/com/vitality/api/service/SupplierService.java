package com.vitality.api.service;

import com.vitality.api.entities.Supplier;
import com.vitality.api.repositories.SupplierRepository;
import com.vitality.common.dtos.CreateSupplierRequest;
import com.vitality.common.exceptions.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    /**
     * Method to search for the supplier Name.
     *
     * @param supplierName: the name of the supplier to be searched.
     *                      The search is performed using a like query, so the supplierName can be a partial name.
     * @return the {@link List<Supplier>} matching the search criteria.
     */
    public List<Supplier> searchSupplier(@NotNull String supplierName) {
        supplierName += "%";
        return supplierRepository.searchBySupplierName(supplierName);
    }

    /**
     * Method to create the supplier.
     *
     * @param request: the supplier Request.
     * @return the supplier id created.
     */
    protected Supplier doCreateSupplier(CreateSupplierRequest request) {
        if (!StringUtils.hasLength(request.getSupplierName())) {
            throw new InvalidRequestException("Supplier name is required to create a supplier.");
        } else {
            List<Supplier> suppliers = searchSupplier(request.getSupplierName());
            if (suppliers != null && !suppliers.isEmpty()) {
                return suppliers.get(0);
            }
            Supplier supplier = new Supplier();
            supplier.setSupplierName(request.getSupplierName());
            supplier.setPocContact(request.getPocName());
            supplier.setPocContact(request.getPocPhone());
            supplier.setSupplierAddress(request.getSupplierAddress());
            supplier.setIsActive(true);
            try {
                supplier = supplierRepository.save(supplier);
            } catch (Exception e) {
                log.error("Failed to create supplier. Error: {}", e.getMessage());
                throw new InvalidRequestException("Failed to create supplier. Please try again later.");
            }
            log.info("Created supplier with id: {} and name: {}", supplier.getId(), supplier.getSupplierName());
            return supplier;
        }
    }

    public Supplier getSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> null);
    }
}
