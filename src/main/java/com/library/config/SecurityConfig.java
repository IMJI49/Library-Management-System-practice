package com.library.config;

import com.library.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Spring Security 설정
 *       - 보안 필터 체인 설정
 *       - 인증 / 인가 규칙 정의
 *       - 로그인/아웃, 회원가입 처리 설정
 */
@Configuration
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity // 메소드 레벨 보안 강화
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomUserDetailsService userDetailsService;
	private final CustomAuthenticationSuccessHandler successHandler;
	private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomLogoutHandler logoutHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;
	/*
	 * 비밀번호 암호화하기
	 * 	- Bcrypt 해시 함수 사용, 단방향 암호화(복호화 불가능)
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		log.info("PasswordEncoder Bean 생성 - BcryptEncoder");
		return new BCryptPasswordEncoder();
	}
    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        log.info("DaoAuthenticationProvider Bean 생성");
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        log.info("1. UserDeTailsService 설정 완료");
        log.info("2. PasswordEncoder 설정 완료");
        return authProvider;
    }
	/*
	 * 보안 필터 체인 설정
	 *     - URL 접근 권한 설정
	 *     - 로그인/아웃 처리 설정
	 *     - CSRF(Cross-Site Request Forgery, 사이트 간 요청 위조), 세션 등 보안 설정
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("=== Spring Security 필터 체인 설정 시작 ===");
		http
					.csrf(csrf -> {
						csrf.disable();
						log.info("1. CSRF 보호 비활성화(운영에서는 활성화 필요)");
					})
					.authenticationProvider(authenticationProvider())
					.authorizeHttpRequests(auth -> {
						auth
						// 누구나 접근 가능(로그인 불필요)
						.requestMatchers("/","/home","/login","/auth/**").permitAll()
						.requestMatchers("/css/**","/js/**","/images/**").permitAll()
						// 그 외 모든 요청은 인증 필요
						.anyRequest().authenticated();
						log.info("2. URL 권한 설정 완료");
					})
					.formLogin(login -> {
						log.info("3. 폼 로그인 설정 완료");
                        login
                                .loginPage("/auth/login")
                                .loginProcessingUrl("/login")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .successHandler(successHandler)
                                .failureHandler(failureHandler)
                                .permitAll();
					})
					.logout(logout -> {
						log.info("4. 로그아웃 설정 완료");
						logout
                                // 로그아웃 요청 URL(POST 방식만 허용)
                                .logoutUrl("/auth/logout")
                                // 커스텀 로그아웃 핸들러 로그아웃 전 실행
                                .addLogoutHandler(logoutHandler)
                                // 로그아웃 성공 후 처리 핸들러
                                .logoutSuccessHandler(logoutSuccessHandler)
                                // 세션 무효화
                                .invalidateHttpSession(true)
                                // JSESSIONID 쿠키 삭제
                                .deleteCookies("JSESSIONID")
                                .logoutSuccessUrl("/auth/login")
                                .permitAll();
                        log.info("로그아웃 설정 완료");
					})
					.exceptionHandling(exception -> {
						log.info("5. 예외처리 설정 완료");
                        exception
                                .accessDeniedPage("/auth/access-denied");
					})
					;
		
		log.info("=== Spring Security 피터 체인 설정 완료 ===");
		return http.build();
	}

}
