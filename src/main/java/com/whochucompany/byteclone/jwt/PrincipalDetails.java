package com.whochucompany.byteclone.jwt;

import com.whochucompany.byteclone.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// 시큐리티가 /login 주소 요청이 오면 낚아채서 로그인을 진행시킨다.
// 로그인을 진행이 완료가 되면 Security ContextHolder 라는 키값에 세션같은 정보를 저장시킨다.
// Security ContextHolder 에는 Authentication 타입의 객체만 들어올 수 있고
// Authentication 안에는 User 정보가 들어있어야 되는데
// 이 User 객체의 타입은 -> UserDetails 타입의 객체이여야 한다.

// ex)
// Security Session => Authentication => UserDetails(PrincipalDetails)

// 여기는 UserDetails 객체
// 즉, 이 인터페이스를 구현하게 되면 Spring Security 에서 구현한 클래스를 사용자 정보로 인식하고 인증작업을 한다.
// 쉽게 이야기 하자면 UserDetails 인터페이스는 VO 역할을 한다. 사용자의 정보를 담아두는 클래스이다.

// 여기는 왜 @Service 같은게 없냐? 닥쳐 나중에 강제로 올린데 나도 몰라 묻지마....
@Getter
public class PrincipalDetails implements UserDetails { // PrincipalDetails 는 UserDetails 타입의 객체이다.

    private Member member;

    public PrincipalDetails(Member member) {
        this.member = member;
    }

    // 해당 유저의 권한목록
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new SimpleGrantedAuthority("ROLE_USER")); // Authority 같은 enum 객체를 따로 만들어야 되나?
        return collection;
    }

    // 비밀번호
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // 보통 PK 값, 중복되지 않는 값 (String)
    @Override
    public String getUsername() {
        return member.getUsername();
    }

    /*
    계정 만료 여부
    true : 만료 안됨
    false : 만료
    @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    /*
      계정 잠김 여부
      true : 잠기지 않음
      false : 잠김
      @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /*
      비밀번호 만료 여부
      true : 만료 안됨
      false : 만료
      @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /*
      사용자 활성화 여부
      ture : 활성화
      false : 비활성화
      @return
     */
    @Override
    public boolean isEnabled()
    {
        // 예를 들어 우리 사이트 회원이 1년동안 휴면 상태이다.
        // 그러면 사용자 loginDate 같은 것을 member Entity 에 만들어 체크하여 true, false
        // 현재시간 - 로그인시간 => 1년 초과시 return false;
        return true;
    }
}
