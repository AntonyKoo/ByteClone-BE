package com.whochucompany.byteclone.domain.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/* *
[빌더 패턴(Builder Pattern)의 장점]

필요한 데이터만 설정할 수 있음
유연성을 확보할 수 있음
가독성을 높일 수 있음
변경 가능성을 최소화할 수 있음

예를 들어 엔티티(Entity) 객체나 도메인(Domain) 객체로부터 DTO를 생성하는 경우라면 직접 빌더를 만들고 하는 작업이 번거로우므로
MapStruct나 Model Mapper와 같은 라이브러리를 통해 생성을 위임할 수 있다. 또한 변수가 늘어날 가능성이 거의 없으며,
변수의 개수가 2개 이하인 경우에는 정적 팩토리 메소드를 사용하는 것이 더 좋을 수도 있다.
빌더의 남용은 오히려 코드를 비대하게 만들 수 있으므로 변수의 개수와 변경 가능성 등을 중점적으로 보고 빌더 패턴을 적용할지 판단하면 된다.
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Member {

    // Todo :: 아직 Timestamped 없음.

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @NotBlank // null, "", " " 전부 허용 안함
    @Column
    private String email;

    @NotBlank
    @Column
    private String username;

    @NotBlank
    @Column
    private String password;
}
