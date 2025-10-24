package com.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/*
    웹 어플리 케이션 전역 설정
        - HTTP 메소드 오버라이드 지원
    HiddenHttpMethodFilter Bean 등록
        - 역할
            - HTML form은 GET, POST만 지원
            - PUT, PATCH, DELETE를 사용할 수 없음
            - _method 파라미터로 실제 http 메소드를 전달함
 */
@Configuration
public class WebConfig {
    @Bean
    HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
