package com.library.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    /*
            로그아웃 성공 후 실행되는 메소드
                 - 실행 시점
                     - 사용자가 로그아웃 요청
                     - 세션 무효화 후에 호출
                     - Spring Security가 자동 호출
     */
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
        String referer = request.getHeader("referer");
        log.info("=== 로그아웃 성공 ===");
        log.info("사용자 : {}", authentication.getName());
        log.info("권한 : {}", authentication.getAuthorities());
        log.info("IP 주소 : {}", request.getRemoteAddr());
        log.info("로그아웃 시간 : {}", LocalDateTime.now());
        log.info("마지막 페이지 : {}", referer);
        log.info("=========");
        // 로그아웃 후 로그인 페이지로 리다이렉트
//        response.sendRedirect("/auth/login?logout=true");
        response.sendRedirect(referer);
	}

}
