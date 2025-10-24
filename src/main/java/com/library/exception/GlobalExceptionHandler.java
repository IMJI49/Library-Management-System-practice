package com.library.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/*
    전역 예외 처리 핸들러 - 어플리케이션 전체에서 발생하는 예외를 중앙에서 처리
    @ControllerAdvice
        - Spring의 AOP를 활용한 전역 예외처리 메커니즘
            - 모든 @Controller에서 발생하는 예외를 한 곳에서 처리
            - 코드 중복 제거 및 일관된 에러 응답 제공
            - Controller에서 try~catch 불필요
    예외 처리 우선 순위 (구체적인 것 => 일반적인 것)
        1) InvalidFileException(파일 검증 실패)
        2) MaxUploadSizeExceededException
        3) RuntimeException(일반 런타임 에러)
        4) Exception(모든 예외의 최종 방어선)
 */
@ControllerAdvice   // 모든 Controller에 적용되는 전역 예외 처리
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = { InvalidFileException.class })
    public ModelAndView handleInvalidFileException(InvalidFileException e) {
        log.error("파일 검증 실패 : {}",e.getMessage());
        ModelAndView mv = new ModelAndView("error/file-error");
        mv.addObject("errorTitle","파일 업로드 실패");
        mv.addObject("errorMessage",e.getMessage());
        mv.addObject("errorDetail","다시 시도해주세요. 문제가 계속되면 관리자에게 문의해주세요");
        mv.setStatus(HttpStatus.BAD_REQUEST);
        return mv;
    }
    @ExceptionHandler(value = { MaxUploadSizeExceededException.class })
    public ModelAndView handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("파일 크기 초과 : {}",e.getMessage());
        ModelAndView mv = new ModelAndView("error/file-error");
        mv.addObject("errorTitle","파일 크기 초과");
        mv.addObject("errorMessage","업로드 가능한 최대 파일 크기는 10MB입니다.");
        mv.addObject("errorDetail","더 작은 파일을 선택하거나 파일을 압축 해 주세요");
        mv.setStatus(HttpStatus.BAD_REQUEST);
        return mv;
    }
    @ExceptionHandler(value = { RuntimeException.class })
    public ModelAndView handleRuntimeException(RuntimeException e) {
        log.error("런타임 예외 발생 : {}",e.getMessage());
        ModelAndView mv = new ModelAndView("error/file-error");
        mv.addObject("errorTitle","오류발생");
        mv.addObject("errorMessage",e.getMessage());
        mv.addObject("errorDetail","요청을 처리하는 중 오류가 발생 했습니다.");
        mv.setStatus(HttpStatus.BAD_REQUEST);
        return mv;
    }
    @ExceptionHandler(value = { Exception.class })
    public ModelAndView handleException(Exception e) {
        log.error("그 외 예외 : {}",e.getMessage());
        ModelAndView mv = new ModelAndView("error/file-error");
        mv.addObject("errorTitle","시스템 오류 발생");
        mv.addObject("errorMessage","일시적인 오류가 발생했습니다.");
        mv.addObject("errorDetail","잠시 후 다시 시도해주세요.");
        mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mv;
    }
}
