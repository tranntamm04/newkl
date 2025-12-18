    package com.example.kltn.service.impl;

    import com.example.kltn.dto.request.*;
    import com.example.kltn.dto.response.AuthResponse;
    import com.example.kltn.entity.PasswordResetToken;
    import com.example.kltn.entity.Role;
    import com.example.kltn.entity.User;
    import com.example.kltn.exception.UserAlreadyExistsException;
    import com.example.kltn.exception.ResourceNotFoundException;
    import com.example.kltn.repository.RoleRepository;
    import com.example.kltn.repository.UserRepository;
    import com.example.kltn.repository.PasswordResetTokenRepository;
    import com.example.kltn.service.AuthService;
    import com.example.kltn.service.EmailService;
    import com.example.kltn.service.JwtService;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.HashSet;
    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final EmailService emailService;
        
        @Value("${app.reset-password.token-expiry-minutes}")
        private int resetPasswordTokenExpiryMinutes;
        
        @Value("${app.verification.token-expiry-minutes}")
        private int verificationTokenExpiryMinutes;

        @Override
        @Transactional
        public AuthResponse register(RegisterRequest request) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email đã được sử dụng!");
            }
            
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .isActive(true)
                    .emailVerified(false)
                    .build();
            
            // Gán role USER mặc định
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại!"));
            user.setRoles(new HashSet<>());
            user.getRoles().add(userRole);
            
            // Tạo verification code
            String verificationCode = UUID.randomUUID().toString();
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationTokenExpiryMinutes));
            
            userRepository.save(user);
            
            // Gửi email xác thực
            // emailService.sendVerificationEmail(user.getEmail(), verificationCode);
            
            String jwtToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());
            
            return AuthResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(user.getRoles().stream().map(Role::getName).toList())
                    .build();
        }

        @Override
        public AuthResponse login(LoginRequest request) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Email hoặc mật khẩu không đúng!"));
            
            if (!user.getIsActive()) {
                throw new RuntimeException("Tài khoản đã bị khóa!");
            }
            
            String jwtToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());
            
            return AuthResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(user.getRoles().stream().map(Role::getName).toList())
                    .build();
        }

        @Override
        @Transactional
        public void forgotPassword(String email) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại!"));
            
            // Tạo token reset password
            String token = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(resetPasswordTokenExpiryMinutes);
            
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(expiryDate)
                    .isUsed(false)
                    .build();
            
            passwordResetTokenRepository.save(resetToken);
            
            // Gửi email reset password
            emailService.sendResetPasswordEmail(user.getEmail(), token);
        }

        @Override
        @Transactional
        public void resetPassword(ResetPasswordRequest request) {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> new ResourceNotFoundException("Token không hợp lệ!"));
            
            if (resetToken.getIsUsed()) {
                throw new RuntimeException("Token đã được sử dụng!");
            }
            
            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Token đã hết hạn!");
            }
            
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            resetToken.setIsUsed(true);
            passwordResetTokenRepository.save(resetToken);
            
            // Xóa tất cả các token cũ của user
            passwordResetTokenRepository.deleteAllByUserAndIsUsed(user, true);
        }

        @Override
        @Transactional
        public void verifyEmail(String verificationCode) {
            User user = userRepository.findByVerificationCode(verificationCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Mã xác thực không hợp lệ!"));
            
            if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Mã xác thực đã hết hạn!");
            }
            
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);
        }

        @Override
        public AuthResponse refreshToken(String refreshToken) {
            String email = jwtService.extractUsername(refreshToken);
            
            if (!jwtService.isTokenValid(refreshToken, email)) {
                throw new RuntimeException("Refresh token không hợp lệ!");
            }
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại!"));
            
            String newJwtToken = jwtService.generateToken(user.getEmail());
            String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());
            
            return AuthResponse.builder()
                    .token(newJwtToken)
                    .refreshToken(newRefreshToken)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(user.getRoles().stream().map(Role::getName).toList())
                    .build();
        }

        @Override
        public void logout(String token) {
            jwtService.invalidateToken(token);
        }

        @Override
        public void resendVerificationEmail(String email) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại!"));
            
            if (user.getEmailVerified()) {
                throw new RuntimeException("Email đã được xác thực!");
            }
            
            String verificationCode = UUID.randomUUID().toString();
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationTokenExpiryMinutes));
            userRepository.save(user);
            
            // emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        }

        @Override
        public void linkOAuth2Account(String provider, String accessToken, String jwtToken) {
            // Implement logic to link OAuth2 account
        }

        @Override
        public void unlinkOAuth2Account(String provider, String jwtToken) {
            // Implement logic to unlink OAuth2 account
        }
    }