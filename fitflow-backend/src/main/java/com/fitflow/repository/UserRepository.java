package com.fitflow.repository;

import com.fitflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data JPA gera a implementação automaticamente em runtime.
// findByEmail deriva a query SQL do nome do método — sem código extra.
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
