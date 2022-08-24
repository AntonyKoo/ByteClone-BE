package com.whochucompany.byteclone.jwt;

import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 여기는 Authentication 객체
// 시큐리티 설정에서 loginProcessingURl("/login");
// /login 요청이 오면 자동으로 UserDetailsService 타입으로 IoC 되어 있는 loadUserByUsername 함수가 실행
// 이건 그냥 규칙임

// UserDetails 가 사용자의 정보를 담았다면 UserDetailsService 는 DB 에서 유저 정보를 불러온다.
// 이 메서드가 loadUserByUsername() 이다.
// 다시 말하자면 DB 에서 유저정보를 가지고와서 return 해주는 작업이다.

// @Service 를 해줘야 IoC 로 등록이 되고 그래야 loadUserByUsername() 오버라이드 사용가능
// http://localhost:8080/login <-- 근데 우리 SecurityConfig 에 httpBasic.disable() 했으니 /login 으로는 동작안함 ㅋ
@RequiredArgsConstructor
@Service
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // UserDetails return 은 어디로 가나?
    // security Session => Authentication => UserDetails(PrincipalDetails)
    // 이렇게 return 해주면
    // security Session(내부 Authentication(내부 UserDetails)) 요롷게 쏘옥 들어간다.
    // 이러면 로그인 완료~
    @Override // String email 에서 email 의 매개변수명은 전달해주는 인자명과 동일해야한다.
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Member> existMember = memberRepository.findByEmail(email);
        return existMember
                .map(PrincipalDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("회원정보를 찾을 수 없습니다."));
    }
}
