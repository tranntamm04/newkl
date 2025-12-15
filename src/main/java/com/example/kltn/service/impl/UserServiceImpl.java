package com.example.kltn.service.impl;

import com.example.kltn.dto.request.UpdateProfileRequest;
import com.example.kltn.dto.response.UserResponse;
import com.example.kltn.entity.Role;
import com.example.kltn.entity.User;
import com.example.kltn.exception.ResourceNotFoundException;
import com.example.kltn.repository.RoleRepository;
import com.example.kltn.repository.UserRepository;
import com.example.kltn.security.CustomUserDetails;
import com.example.kltn.service.JwtService;
import com.example.kltn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
        
        return new CustomUserDetails(user);
    }
    
    @Override
    public UserResponse getCurrentUser(String token) {
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        return mapToUserResponse(user);
    }
    
    @Override
    public User getCurrentUserEntity(String token) {
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }
    
    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request, String token) {
        User user = getCurrentUserEntity(token);
        
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            user.setAvatar(request.getAvatar());
        }
        
        userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return mapToUserResponse(user);
    }
    
    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return mapToUserResponse(user);
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserResponse> getUsersByRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        
        return userRepository.findByRolesContaining(role).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
            roles.add(role);
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Override
    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Override
    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsActive(true);
        userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void processOAuthPostLogin(String email, String name, String picture) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .fullName(name)
                    .avatar(picture)
                    .emailVerified(true)
                    .isActive(true)
                    .provider(User.AuthProvider.GOOGLE)
                    .build();
            
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));
            user.setRoles(new HashSet<>());
            user.getRoles().add(userRole);
            
            userRepository.save(user);
        }
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .provider(user.getProvider().name())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}