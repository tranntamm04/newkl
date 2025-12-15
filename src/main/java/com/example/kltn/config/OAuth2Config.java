package com.example.kltn.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuth2Config {

    private final OAuth2ClientProperties oAuth2ClientProperties;

    public OAuth2Config(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        
        // Google OAuth2
        if (oAuth2ClientProperties.getRegistration().containsKey("google")) {
            OAuth2ClientProperties.Registration googleRegistration = 
                oAuth2ClientProperties.getRegistration().get("google");
            
            ClientRegistration google = ClientRegistration
                .withRegistrationId("google")
                .clientId(googleRegistration.getClientId())
                .clientSecret(googleRegistration.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
            
            registrations.add(google);
        }
        
        // Facebook OAuth2
        if (oAuth2ClientProperties.getRegistration().containsKey("facebook")) {
            OAuth2ClientProperties.Registration facebookRegistration = 
                oAuth2ClientProperties.getRegistration().get("facebook");
            
            ClientRegistration facebook = ClientRegistration
                .withRegistrationId("facebook")
                .clientId(facebookRegistration.getClientId())
                .clientSecret(facebookRegistration.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("email", "public_profile")
                .authorizationUri("https://www.facebook.com/v12.0/dialog/oauth")
                .tokenUri("https://graph.facebook.com/v12.0/oauth/access_token")
                .userInfoUri("https://graph.facebook.com/v12.0/me?fields=id,name,email,picture")
                .userNameAttributeName("id")
                .clientName("Facebook")
                .build();
            
            registrations.add(facebook);
        }
        
        return new InMemoryClientRegistrationRepository(registrations);
    }
}