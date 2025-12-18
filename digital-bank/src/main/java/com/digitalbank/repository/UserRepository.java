package com.digitalbank.repository;

import com.digitalbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByUsername(String username);
    boolean existsByUserId(String userId);
    boolean existsByIdCard(String idCard);
}