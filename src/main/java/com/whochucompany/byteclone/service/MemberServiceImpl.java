package com.whochucompany.byteclone.service;

import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.domain.member.dto.LoginRequestDto;
import com.whochucompany.byteclone.domain.member.dto.MemberRequestDto;
import com.whochucompany.byteclone.domain.member.dto.MemberResponseDto;
import com.whochucompany.byteclone.domain.token.JwtTokenDto;
import com.whochucompany.byteclone.jwt.PrincipalDetails;
import com.whochucompany.byteclone.jwt.TokenProvider;
import com.whochucompany.byteclone.logging.Logging;
import com.whochucompany.byteclone.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    // email 중복 체크
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<?> emailExist(String email) {
        boolean result = memberRepository.existsByEmail(email);
        if (result) { // 결과가 존재하면 참 --> result 가 true 면...
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.entry("result", "false"));
        }
        else {
            return ResponseEntity.ok().body(Map.entry("result", "true"));
        }
    }

    // 회원가입
    @Transactional
    @Override
    public MemberResponseDto saveUser(MemberRequestDto memberRequestDto) {

        // 패스워드 암호화
        String password = passwordEncoder.encode(memberRequestDto.getPassword());

        // 새로운 멤버 객체를 생성하자~
        Member member = Member.builder()
                .email(memberRequestDto.getEmail())
                .username(memberRequestDto.getUsername())
                .password(password)
                .build();

        try {
            // 회원 저장
            memberRepository.save(member);
            log.info("msg : {}님 회원가입이 성공", member.getUsername());

        } catch (Exception e) {
            String error = Logging.getPrintStackTrace(e);
            log.error(error);
        }
        // Todo :: 트랜잭션 전 @id 값을 어떻게 가져오는지.. insert query 를 날리는지 확인.
        // return 할 MemberResponseDto
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .build();
    }

    // 회원 로그인
    @Override
    public JwtTokenDto login(LoginRequestDto loginRequestDto) {

        String email = loginRequestDto.getEmail();

        // 데이터베이스에 저장된 사용자 찾아오기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
                });

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        PrincipalDetails principalDetails = new PrincipalDetails(member); // member 를 이용해서 Authentication 객체 만들기

//        UsernamePasswordAuthenticationFilter → username, password 를 쓰는 form 기반 인증을 처리하는 필터.
//
//                AuthenticationManager 를 통한 인증 실행
//        성공하면, Authentication 객체를 SecurityContext 에 저장 후 AuthenticationSuccessHandler 실행
//        실패하면 AuthenticationFailureHandler 실행

        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "");

        JwtTokenDto jwtTokenDto = tokenProvider.generateTokenDto(authentication);

        return jwtTokenDto;
    }

    // 회원 로그아웃
    public ResponseEntity<?> logout(HttpServletRequest request) {
        if (!tokenProvider.validationRefreshToken(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("토큰 정보를 찾을 수 없습니다.");
        }
        Member member = tokenProvider.getMemberFromAuthentication(); // SecurityContextHolder 에 있는 회원정보를 가져옴
        if (null == member) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
        return tokenProvider.deleteRefreshToken(member);
    }

    // 그냥 멤버 가져오는거
    @Override
    public MemberResponseDto getMember(String username) {

        Optional<Member> member = memberRepository.findByUsername(username);

        return null;
    }
}
