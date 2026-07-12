package org.socialmedia.app.controller;

import jakarta.validation.Valid;
import org.socialmedia.app.dto.ApiResponse;
import org.socialmedia.app.dto.auth.LoginRequest;
import org.socialmedia.app.dto.auth.SignupRequest;
import org.socialmedia.app.dto.auth.VerificationRequest;
import org.socialmedia.app.dto.auth.ForgotPasswordRequest;
import org.socialmedia.app.dto.auth.ResetPasswordRequest;
import org.socialmedia.app.dto.auth.ResendVerificationRequest;
import org.socialmedia.app.dto.auth.ChangePasswordRequest;
import org.socialmedia.app.dto.auth.TokenRefreshRequest;
import org.socialmedia.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.authenticateUser(loginRequest), "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(ApiResponse.success(null, "User registered successfully. Please check your email for the verification code."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody VerificationRequest verificationRequest) {
        authService.verifyEmail(verificationRequest);
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully. You can now login."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset code sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password has been successfully reset."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser(), "Current user details"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<?>> resendVerificationEmail(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null, "A new verification code has been sent to your email."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully. You will need to log in again."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request), "Token refreshed successfully"));
    }
}
