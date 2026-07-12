package org.socialmedia.app.service;

import org.socialmedia.app.dao.RefreshTokenDAO;
import org.socialmedia.app.dao.UserDAO;
import org.socialmedia.app.model.auth.RefreshToken;
import org.socialmedia.app.exception.ResourceNotFoundException;
import org.socialmedia.app.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenDAO refreshTokenDAO;
    private final UserDAO userDAO;

    public RefreshTokenService(RefreshTokenDAO refreshTokenDAO, UserDAO userDAO) {
        this.refreshTokenDAO = refreshTokenDAO;
        this.userDAO = userDAO;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userDAO.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found")));
        refreshToken.setTokenHash(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        return refreshTokenDAO.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenDAO.delete(token);
            throw new UnauthorizedException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenDAO.findByTokenHash(tokenHash);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenDAO.deleteByUser_Id(userId);
    }
}
