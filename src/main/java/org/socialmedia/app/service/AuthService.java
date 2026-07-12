package org.socialmedia.app.service;

import org.socialmedia.app.dao.RoleDAO;
import org.socialmedia.app.dao.UserDAO;
import org.socialmedia.app.dao.VerificationTokenDAO;
import org.socialmedia.app.dto.auth.*;
import org.socialmedia.app.exception.BadRequestException;
import org.socialmedia.app.exception.ConflictException;
import org.socialmedia.app.exception.ResourceNotFoundException;
import org.socialmedia.app.exception.UnauthorizedException;
import org.socialmedia.app.dto.user.UserResponse;
import org.socialmedia.app.model.auth.RefreshToken;
import org.socialmedia.app.model.auth.VerificationToken;
import org.socialmedia.app.model.auth.VerificationTokenType;
import org.socialmedia.app.model.security.Role;
import org.socialmedia.app.model.user.User;
import org.socialmedia.app.model.user.UserStatus;
import org.socialmedia.app.security.CustomUserDetails;
import org.socialmedia.app.util.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final VerificationTokenDAO verificationTokenDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager,
                       UserDAO userDAO,
                       RoleDAO roleDAO,
                       VerificationTokenDAO verificationTokenDAO,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       EmailService emailService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.verificationTokenDAO = verificationTokenDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication.getName());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (userDetails == null) throw new UnauthorizedException("Invalid user details");
        if (userDetails.getUser().getEmailVerifiedAt() == null) {
            throw new UnauthorizedException("Email not verified");
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        refreshTokenService.deleteByUserId(userDetails.getUser().getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser().getId());

        return new JwtResponse(
                jwt,
                refreshToken.getTokenHash(),
                userDetails.getUser().getId(),
                userDetails.getUsername(),
                userDetails.getUser().getEmail(),
                roles
        );
    }

    @Transactional
    public void registerUser(SignupRequest signUpRequest) {
        if (userDAO.existsByUsername(signUpRequest.getUsername())) {
            throw new ConflictException("Error: Username is already taken!");
        }

        if (userDAO.existsByEmail(signUpRequest.getEmail())) {
            throw new ConflictException("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setStatus(UserStatus.PENDING);

        Set<Role> roles = new HashSet<>();
        Role userRole = roleDAO.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        userDAO.save(user);

        String code = generateCode();
        createVerificationToken(user, code, VerificationTokenType.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    @Transactional
    public void verifyEmail(VerificationRequest request) {
        VerificationToken token = verificationTokenDAO
                .findByUserEmailAndCodeAndType(request.getEmail(), request.getCode(), VerificationTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("Invalid verification code"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code expired");
        }

        if (token.getUser() == null) throw new ResourceNotFoundException("User not found for token");
        User user = token.getUser();
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
        userDAO.save(user);

        verificationTokenDAO.delete(token);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verificationTokenDAO.deleteByUserIdAndType(user.getId(), VerificationTokenType.PASSWORD_RESET);

        String code = generateCode();
        createVerificationToken(user, code, VerificationTokenType.PASSWORD_RESET);
        emailService.sendPasswordResetEmail(user.getEmail(), code);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        VerificationToken token = verificationTokenDAO
                .findByUserEmailAndCodeAndType(request.getEmail(), request.getCode(), VerificationTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("Invalid reset code"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset code expired");
        }

        if (token.getUser() == null) throw new ResourceNotFoundException("User not found for token");
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userDAO.save(user);

        verificationTokenDAO.delete(token);
    }

    private void createVerificationToken(User user, String code, VerificationTokenType type) {
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setCode(code);
        token.setType(type);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        verificationTokenDAO.save(token);
    }

    private String generateCode() {
        int number = secureRandom.nextInt(999999);
        return String.format("%06d", number);
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            return UserResponse.fromUser(userDetails.getUser(), roles);
        }
        throw new UnauthorizedException("Invalid user details");
    }

    @Transactional
    public void resendVerificationEmail(ResendVerificationRequest request) {
        User user = userDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmailVerifiedAt() != null) {
            throw new BadRequestException("User is already verified");
        }

        verificationTokenDAO.findByUserIdAndType(user.getId(), VerificationTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("No existing verification code found to resend. Please register again."));

        verificationTokenDAO.deleteByUserIdAndType(user.getId(), VerificationTokenType.EMAIL_VERIFICATION);

        String code = generateCode();
        createVerificationToken(user, code, VerificationTokenType.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails == null) throw new UnauthorizedException("Invalid user details");
        User user = userDetails.getUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userDAO.save(user);

        refreshTokenService.deleteByUserId(user.getId());
    }

    @Transactional
    public JwtResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByTokenHash(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtToken(user.getEmail());
                    List<String> roles = user.getRoles().stream()
                            .map(Role::getName)
                            .toList();
                    return new JwtResponse(token, requestRefreshToken, user.getId(), user.getUsername(), user.getEmail(), roles);
                })
                .orElseThrow(() -> new UnauthorizedException("Refresh token is not in database!"));
    }
}
