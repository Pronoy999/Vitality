package com.vitality.common.dtos;

import lombok.Data;

@Data
public class ParsedMedicineData {
    private String name;
    private String dosage;
    private Integer quantity;
}
