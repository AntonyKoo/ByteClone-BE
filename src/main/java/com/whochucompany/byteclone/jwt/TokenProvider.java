package com.whochucompany.byteclone.jwt;

import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.domain.token.JwtTokenDto;
import com.whochucompany.byteclone.domain.token.RefreshToken;
import com.whochucompany.byteclone.logging.Logging;
import com.whochucompany.byteclone.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // accessToken exp 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // refreshToken exp 7일

    private final Key key; // 서명 부문에 header + payload + key 해서 암호화 할거임~

    private final PrincipalDetailsService principalDetailsService;

    private final RefreshTokenRepository refreshTokenRepository; // 리프레쉬 토큰은 일단 나중에 하쟈..

    // TokenProvider 생성자.. secretKey 생성
    public TokenProvider(@Value("${jwt.secret}") String secretKey,
                         PrincipalDetailsService principalDetailsService, RefreshTokenRepository refreshTokenRepository) {

        // 로직 안에서는 byte 단위의 secretKey 를 만들어 주어야 한다.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // 알고리즘 선택
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.principalDetailsService = principalDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Token 생성
    public JwtTokenDto generateTokenDto(Authentication authentication) {

        Member member = ((PrincipalDetails) authentication.getPrincipal()).getMember();

        long now = (new Date()).getTime(); // expire 시간을 지정하기 위해 현재 시간을 가져온다.
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME); // 토큰 만료 시간 설정
        Date refreshTokenExpiresIn = new Date(now + REFRESH_TOKEN_EXPIRE_TIME); // 리프레쉬 토큰 만료 시간 설정

        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "HS256");
        headers.put("typ", "Bearer");

        // payload 에 들어갈 것 커스텀
        Map<String, String> claims = new HashMap<>();
        claims.put("memberEmail", member.getEmail());
        claims.put("memberUsername", member.getUsername());

        // AccessToken 생성
        String accessToken = Jwts.builder()
                .setHeader(headers)
                .setClaims(claims)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 데이터베이스 저장
        RefreshToken refreshTokenObj = RefreshToken.builder()
                .id(member.getId())
                .refreshToken(refreshToken)
                .build();

        refreshTokenObj.updateTokenValue(refreshToken);

        refreshTokenRepository.save(refreshTokenObj);

        // 생성한 토큰 return
        return JwtTokenDto.builder()
                .authorization(BEARER_TYPE + " " + accessToken) // Bearer + " " + accessToken
                .refreshToken(BEARER_TYPE + " " + refreshToken) // Bearer + " " + refreshToken
                .build();
    }

    // header request 에서 전달받은 accessToken 으로 정보 확인하기. --> 여기 원해 권한 정보 가져오는데 지금은 안쓰니까 나중에 다시 체크해봐.
    public Authentication getAuthentication(String accessToken) {

        Claims claims = parseClaims(accessToken);

        String email = (String) claims.get("memberEmail"); // 전달받은 payload 에 key 값이 "memberEmail" 을 찾아 값을 가져옴

        UserDetails principal = principalDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(principal, "", null); // 회원정보,
    }

    public Member getMemberFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return null;
        }
        return ((PrincipalDetails) authentication.getPrincipal()).getMember();
    }

    // 토큰 유효성 검증
    public boolean validationToken(String token) {

        try {
            // 위에서 암호환한 Jwts를 복호화 해줌
            // 위에서 signWith key 를 활용하여 암호화 했으므로 복호활 할 setSigningKey 에도 동일한 key 값을 넣어줌
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalStateException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public boolean validationRefreshToken(HttpServletRequest request) {

        try {
            String token = resolveRefreshToken(request);
            // 위에서 암호환한 Jwts를 복호화 해줌
            // 위에서 signWith key 를 활용하여 암호화 했으므로 복호활 할 setSigningKey 에도 동일한 key 값을 넣어줌
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalStateException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        // 헤더에서 refreshToken 추출
        String refreshToken = request.getHeader("Refresh-Token");

        if (StringUtils.hasText(refreshToken) && refreshToken.startsWith("Bearer ")) {
            return refreshToken.substring(7);
        }
        return null;
    }



    // 토큰 복호화
    // Todo :: 나 이 부분 이해 안되..
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

        } catch (ExpiredJwtException e) {
            String error = Logging.getPrintStackTrace(e);
            log.error(error);
            return e.getClaims();
        }
    }

    @Transactional
    public ResponseEntity<?> deleteRefreshToken(Member member) {
        RefreshToken refreshToken = isPresentRefreshToken(member);
        if (refreshToken == null) {
            return ResponseEntity.notFound().build();
        }
        refreshTokenRepository.delete(refreshToken);
        return ResponseEntity.ok().body("로그아웃 되셨습니다.");
    }

    @Transactional(readOnly = true)
    public RefreshToken isPresentRefreshToken(Member member) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(member.getId());
        return optionalRefreshToken.orElse(null);
    }
}
