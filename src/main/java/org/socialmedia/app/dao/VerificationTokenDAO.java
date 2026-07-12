package org.socialmedia.app.dao;

import org.socialmedia.app.model.auth.VerificationToken;
import org.socialmedia.app.model.auth.VerificationTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenDAO extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByCodeAndType(String code, VerificationTokenType type);
    Optional<VerificationToken> findByUserEmailAndCodeAndType(String email, String code, VerificationTokenType type);
    Optional<VerificationToken> findByUserIdAndType(Long userId, VerificationTokenType type);
    void deleteByUserIdAndType(Long userId, VerificationTokenType type);
}
