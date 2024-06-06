package com.example.reminderservicemaven.controllers;

import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/showUserInfo")
    public String showUsername(@AuthenticationPrincipal OAuth2User principal, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String jSessionId = "";
        if (cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("JSESSIONID")) {
                    jSessionId = cookie.getValue();
                }
            }
        }

        return principal.getAttribute("name") +  " " +jSessionId;
    }
}
