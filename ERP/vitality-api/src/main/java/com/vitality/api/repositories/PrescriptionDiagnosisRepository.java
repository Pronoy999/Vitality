package com.vitality.api.repositories;

import com.vitality.api.entities.PrescriptionDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionDiagnosisRepository extends JpaRepository<PrescriptionDiagnosis, Long> {
}
