package com.lightbalance.health.web;

import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AppDtos.AuthResponse register(@RequestBody AppDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AppDtos.AuthResponse login(@RequestBody AppDtos.AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AppDtos.AuthResponse me(HttpServletRequest request) {
        return authService.me(request);
    }

    @PostMapping("/change-password")
    public AppDtos.AuthResponse changePassword(
        HttpServletRequest request,
        @RequestBody AppDtos.ChangePasswordRequest passwordRequest
    ) {
        return authService.changePassword(request, passwordRequest);
    }

    @PostMapping("/recover-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recoverPassword(@RequestBody AppDtos.RecoverPasswordRequest request) {
        authService.recoverPassword(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        authService.logout(request);
    }
}
