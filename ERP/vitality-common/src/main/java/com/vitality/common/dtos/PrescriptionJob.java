package com.vitality.common.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;
@Data
@Getter
@Setter
public class PrescriptionJob {
    private final String id;
    private volatile String status;
    private volatile String step;
    private volatile ParsedPrescriptionData data;
    private volatile String error;
    private final List<Path> imagePaths;

    public PrescriptionJob(String id, String status, String step, List<Path> imagePaths) {
        this.id = id;
        this.status = status;
        this.step = step;
        this.imagePaths = imagePaths;
    }
}