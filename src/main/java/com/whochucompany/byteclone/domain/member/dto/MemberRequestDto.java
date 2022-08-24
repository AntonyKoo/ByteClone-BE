package com.whochucompany.byteclone.domain.member.dto;

import lombok.Getter;

@Getter
public class MemberRequestDto {
    private String email;
    private String username;
    private String password;
}
