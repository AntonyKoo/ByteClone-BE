package com.whochucompany.byteclone.controller;

import com.amazonaws.services.kms.model.NotFoundException;
import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.domain.member.dto.LoginRequestDto;
import com.whochucompany.byteclone.domain.member.dto.MemberRequestDto;
import com.whochucompany.byteclone.domain.member.dto.MemberResponseDto;
import com.whochucompany.byteclone.domain.token.JwtTokenDto;
import com.whochucompany.byteclone.repository.MemberRepository;
import com.whochucompany.byteclone.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping("/user")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    // ResponseEntity 는 HttpEntity 를 상속받음으로써 Http Status, Header 와 Body 를 가질 수 있다.

    // email 이메일 체크
    @GetMapping("/emailCheck/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) throws IllegalArgumentException{ // 인자값 제대로 안들어오면 IllegalArgumentException
        return memberService.emailExist(email);
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody MemberRequestDto memberRequestDto) throws IllegalArgumentException {

        boolean result = memberRepository.existsByEmail(memberRequestDto.getEmail());

        if (result) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.entry("message", "등록된 이메일이 존재합니다."));
        }

        MemberResponseDto memberResponseDto = memberService.saveUser(memberRequestDto);

//        return ResponseEntity
//                .status(HttpStatus.CREATED) // 회원가입 성공시  201 created
//                .body(memberRequestDto);

        return new ResponseEntity<>(memberResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        JwtTokenDto jwtTokenDto = memberService.login(loginRequestDto);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", jwtTokenDto.getAuthorization());
        headers.add("Refresh-Token", jwtTokenDto.getRefreshToken());
        Member member = memberRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을수 없습니다."));
        return ResponseEntity.ok().headers(headers)
                .body(Map.entry("username", member.getUsername()));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return memberService.logout(request);
    }

}
