package com.vitality.api.repositories;

import com.vitality.api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT u FROM User u WHERE u.guid = :guid")
    User findUserByGuidIs(String guid);
}
