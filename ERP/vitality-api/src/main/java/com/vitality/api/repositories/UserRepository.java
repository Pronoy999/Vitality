package com.vitality.api.repositories;

import com.vitality.api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT u FROM User u WHERE u.guid = :guid and u.isActive=true")
    User findUserByGuidIs(String guid);

    @Query("SELECT u from User u where u.firstName=:firstName and u.lastName=:lastName and u.isActive=true")
    User findByFirstNameAndLastName(String firstName, String lastName);
}
