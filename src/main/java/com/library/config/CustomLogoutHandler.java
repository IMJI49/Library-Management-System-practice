package com.library.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomLogoutHandler implements LogoutHandler {
    /*
           로그아웃 전에 실행되는 메소드
                - 실행 시점
                    - 사용자가 로그아웃 요청
                    - 세션 무효화 전에 호출
                    - Spring Security가 자동 호출
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            // 로그아웃 하는 사용자 정보
            String username = authentication.getName();
            log.info("=== 로그아웃 시도 ===");
            log.info("사용자 : {}", username);
            log.info("권한 : {}", authentication.getAuthorities());
            log.info("IP 주소 : {}", request.getRemoteAddr());
            log.info("로그아웃 시간 : {}", LocalDateTime.now());
            // 세션 정보 로깅
            if (request.getSession(false) != null) {
                log.info("세션 ID : {}", request.getSession().getId());
                log.info("세션 무효화 예정");
            }
            log.info("=========");
        } else {
            log.warn("⚠️ 로그아웃 시도 - 인증 정보 없음");
        }
    }
}
