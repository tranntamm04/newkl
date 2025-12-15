package com.example.kltn.repository;

import com.example.kltn.entity.Role;
import com.example.kltn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    Optional<User> findByVerificationCode(String verificationCode);
    
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    
    List<User> findByRolesContaining(Role role);
    
    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.providerId = :providerId")
    Optional<User> findByProviderAndProviderId(@Param("provider") User.AuthProvider provider, 
                                               @Param("providerId") String providerId);
    
    List<User> findByIsActive(Boolean isActive);
    
    List<User> findByEmailVerified(Boolean emailVerified);
}