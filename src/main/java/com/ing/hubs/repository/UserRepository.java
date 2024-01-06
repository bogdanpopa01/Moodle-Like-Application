package com.ing.hubs.repository;

import com.ing.hubs.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(final String username);
    boolean existsByUsername(final String username);
    boolean existsByEmail(final String email);
    boolean existsByPhoneNumber(final String phoneNumber);

}
