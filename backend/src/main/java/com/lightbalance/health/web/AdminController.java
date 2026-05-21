package com.lightbalance.health.web;

import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.service.AdminService;
import com.lightbalance.health.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping("/users")
    public AppDtos.AdminUsersResponse getUsers(HttpServletRequest request) {
        return adminService.getUsers(authService.requireCurrentUser(request));
    }

    @PostMapping("/users/{userId}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetUserPassword(
        HttpServletRequest request,
        @PathVariable Long userId,
        @RequestBody AppDtos.AdminResetPasswordRequest body
    ) {
        adminService.resetUserPassword(authService.requireCurrentUser(request), userId, body);
    }
}
