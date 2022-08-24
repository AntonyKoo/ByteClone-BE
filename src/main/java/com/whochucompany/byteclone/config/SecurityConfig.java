package com.whochucompany.byteclone.config;

import com.whochucompany.byteclone.jwt.JwtAuthenticationFilter;
import com.whochucompany.byteclone.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;


@Configuration // 설정파일을 만들거나 bean 등록을 위한 어노테이션
@EnableWebSecurity //SecurityFilterChain 등 WebSecurity 에 필요한 거 어노테이션으로 가져옴, security 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsFilter corsFilter; // CorsConfig 에서 @Bean 으로 등록된 것.
    private final TokenProvider tokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCryptPasswordEncoder 은 PasswordEncoder 의 구현체
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // h2-console 사용에 대한 허용 (CSRF, FrameOptions 무시)
        return (web) -> web.ignoring()
                .antMatchers("/h2-console/**");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // origin
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // method
        configuration.setAllowedMethods(Arrays.asList("*"));
        // header
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Headers", "Authorization, x-xsrf-token, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, " +
                "Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors().configurationSource(corsConfigurationSource());

        http.csrf().disable(); // csrf (Cross site Request forgery) 로 사이트간 위조 요청, 사용한다면 보안에 좋음
                               // GET 를 제외한 상태를 변화시킬 수 있는 나머지를 요청을 차단하여 보호함.
                               // .disable() 한 이유는 rest api 를 이용한 서버라면, session 기반 인증과 다르게
                               // stateless 하기 때문에 서버에 인증정보를 보관하지 않음.
                               // rest api 에서 권한이 필요한 요청을 하기 위해서는 요청에 필요한 인증 정보를 (OAuth2, jwt 토큰 등) 을
                               // 포함시켜야 한다. 그러니 구지 csrf 설정을 해줄 필요가 없다.

        http
                // UsernamePasswordAuthenticationFilter 필터 적용전에 우리가 커스텀한 필터를 먼저 적용
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않겠다.
                .and()
                // .addFilter(corsFilter) // cors 필터 등록, cors 정책에서 벗어날 수 있다.
                                       // RestController 에서 @CrossOrigin 과 다른점은 @CrossOrigin 은 인증 X
                                       // 인증이 필요한 작업은 필터를 직접 등록해주어야 함
                .formLogin().disable()  // 로그인 폼 사용 안함
                .httpBasic().disable(); // headers 에 Authorization 에 Id, pw 가 들어가지만 암호화가 안돼 보안에 취약, 기본 인증 방식 X

        http
                .authorizeRequests()
                .antMatchers("/*").permitAll()
                .anyRequest().permitAll();

        return http.build();
    }
}
