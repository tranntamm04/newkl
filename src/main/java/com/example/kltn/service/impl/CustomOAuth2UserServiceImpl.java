package com.example.kltn.service.impl;

import com.example.kltn.entity.Role;
import com.example.kltn.entity.User;
import com.example.kltn.repository.RoleRepository;
import com.example.kltn.repository.UserRepository;
import com.example.kltn.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = getEmail(provider, attributes);
        String name = getName(provider, attributes);
        String picture = getPicture(provider, attributes);
        String providerId = getProviderId(provider, attributes);
        
        Optional<User> optionalUser = userRepository.findByEmail(email);
        
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            // Cập nhật thông tin từ OAuth2
            if (user.getProvider() == User.AuthProvider.LOCAL) {
                user.setProvider(User.AuthProvider.valueOf(provider.toUpperCase()));
                user.setProviderId(providerId);
                user.setAvatar(picture);
                userRepository.save(user);
            }
        } else {
            // Tạo user mới
            user = createNewUser(email, name, picture, provider, providerId);
        }
        
        return new CustomOAuth2User(oAuth2User, user);
    }
    
    private User createNewUser(String email, String name, String picture, String provider, String providerId) {
        User user = User.builder()
                .email(email)
                .fullName(name)
                .avatar(picture)
                .provider(User.AuthProvider.valueOf(provider.toUpperCase()))
                .providerId(providerId)
                .emailVerified(true)
                .isActive(true)
                .build();
        
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));
        user.setRoles(new HashSet<>());
        user.getRoles().add(userRole);
        
        return userRepository.save(user);
    }
    
    private String getEmail(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("email");
        }
        throw new OAuth2AuthenticationException("Provider không được hỗ trợ");
    }
    
    private String getName(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("name");
        }
        return "";
    }
    
    private String getPicture(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("picture");
        } else if ("facebook".equals(provider)) {
            Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
            if (picture != null) {
                Map<String, Object> data = (Map<String, Object>) picture.get("data");
                if (data != null) {
                    return (String) data.get("url");
                }
            }
        }
        return "";
    }
    
    private String getProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("id");
        }
        return "";
    }
}