package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;

@MappedSuperclass
@Data
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_sid_gen")
    @SequenceGenerator(
            name = "global_sid_gen",
            sequenceName = "global_sid_seq",
            allocationSize = 1
    )
    private Long id;
}
