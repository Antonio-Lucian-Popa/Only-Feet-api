package com.asusoftware.only_feet_api.user.repository;

import com.asusoftware.only_feet_api.user.model.User;
import com.asusoftware.only_feet_api.user.model.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByKeycloakId(UUID keycloakId);
    Optional<User> findByStripeAccountId(String stripeAccountId);

    List<User> findByRole(UserRole role, Pageable pageable);

}