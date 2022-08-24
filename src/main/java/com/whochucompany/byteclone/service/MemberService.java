package com.whochucompany.byteclone.service;

import com.whochucompany.byteclone.domain.member.dto.LoginRequestDto;
import com.whochucompany.byteclone.domain.member.dto.MemberRequestDto;
import com.whochucompany.byteclone.domain.member.dto.MemberResponseDto;
import com.whochucompany.byteclone.domain.token.JwtTokenDto;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface MemberService {
    MemberResponseDto saveUser(MemberRequestDto memberRequestDto);

    JwtTokenDto login(LoginRequestDto loginRequestDto);

    ResponseEntity<?> emailExist(String email);

    MemberResponseDto getMember(String username);

    ResponseEntity<?> logout(HttpServletRequest request);




}
