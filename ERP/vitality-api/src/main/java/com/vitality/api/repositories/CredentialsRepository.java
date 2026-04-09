package com.vitality.api.repositories;

import com.vitality.api.entities.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    @Query("SELECT c FROM Credentials c WHERE c.emailId = :emailId")
    Credentials findCredentialsByEmailId(String emailId);
}
