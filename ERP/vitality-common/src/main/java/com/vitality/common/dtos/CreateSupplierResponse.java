package com.vitality.common.dtos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateSupplierResponse {
    private Long supplierId;
    private String supplierName;
}
