package com.whochucompany.byteclone.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Filter 나 GenericFilterBean 이라는 스프링 확장 필터에서
                                                                    // 서블릿 실행 시 요청이 들어오면 다시 필터부터 동작이 되는데
                                                                    // OncePerRequestFilter 를 사용하면 사용자 요청 한번에 필터를 딱 한번만 돈다.
                                                                    // @Override doFilterInternal 를 구현해야한다.


    // 자 이제 필터를 탈 거니까.. 토큰 검증을 해보자...
    // 시큐리티 필터는 여러개니까 나중에 SecurityConfig 에 먼저 넣고 싶은 곳이 있다면 공부해서 넣으세욥...

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String REFRESH_AUTHORIZATION_HEADER = "RefreshToken";
    public static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1) HttpServletRequest request 에서 Header(jwtToken)를 획득한다.
        String jwtToken = resolveToken(request);

        // 나중에 이부분에서 refreshToken 재발급 로직을 짜보자..

        if (StringUtils.hasText(jwtToken) && tokenProvider.validationToken(jwtToken)) { // jwtToken 에 값이 있고, 토큰 유형성 검증을 통과했을때..
            // jwtToken 으로 부터 Authentication 객체 얻어오기
            Authentication authentication = tokenProvider.getAuthentication(jwtToken);
            // 받아온 Authentication 객체 SecurityContextHolder 에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }


        filterChain.doFilter(request, response); // 모든 연결된 필터에 request 와 response 를 가져간다 이건가??
    }

    // header 에서 토큰을 뽑는 메서드
    private String resolveToken(HttpServletRequest request) {
        // authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // Authorization
        // 접두사 분리
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) { // 토큰에 값이 있고, 토큰 시작이 "Bearer " 일때
            return bearerToken.substring(7); // "Bearer " + 토큰 정보에서 "Bearer " 를 땜
        }
        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        // 헤더에서 refreshToken 추출
        String refreshToken = request.getHeader(REFRESH_AUTHORIZATION_HEADER);

        if (StringUtils.hasText(refreshToken) && refreshToken.startsWith(BEARER_PREFIX)) {
            return refreshToken.substring(7);
        }
        return null;
    }
}
