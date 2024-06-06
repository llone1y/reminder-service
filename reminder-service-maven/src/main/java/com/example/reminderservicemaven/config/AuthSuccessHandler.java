package com.example.reminderservicemaven.config;

import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.repositories.UserRepository;
import com.example.reminderservicemaven.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String username = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        userService.saveUser(user);
        response.sendRedirect("/user/showUserInfo");
    }
}
