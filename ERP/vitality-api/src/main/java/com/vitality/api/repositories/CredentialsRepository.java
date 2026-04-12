package com.vitality.api.repositories;

import com.vitality.api.entities.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    @Query("SELECT c FROM Credentials c WHERE c.emailId = :emailId and c.isActive=true")
    Credentials findCredentialsByEmailId(String emailId);

    @Query("SELECT c FROM Credentials c WHERE c.phoneNumber = :phoneNumber or c.emailId=:emailId and c.isActive=true")
    Credentials findByEmailIdOrPhoneNumber(String emailId, String phoneNumber);
}
