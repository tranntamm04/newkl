package com.example.kltn.repository;

import com.example.kltn.entity.PasswordResetToken;
import com.example.kltn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    void deleteAllByUserAndIsUsed(User user, Boolean isUsed);
    
    void deleteByUser(User user);
}