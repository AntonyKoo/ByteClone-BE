package com.whochucompany.byteclone.repository;

import com.whochucompany.byteclone.domain.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsByRefreshToken(String refreshToken);

    Optional<RefreshToken> findById(Long memberId);
}
