package com.library.exception;
/*
    파일 업로드 검증 실패 시 발생하는 커스텀 예외

    RuntimeException 상속
        - unchecked exception으로 동작 (try~catch 강제 안함)
        - Spring의 @ControllerAdvice로 전역 처리 가능
        - 비지니스 로직에서 예외 처리 코드 간소화
    발생 상황
        - 허용되지 않은 파일 확장자 업로드
        - 파일 크기 제한 초과
        - 파일명 비어있거나 null인 경우
        - MIME 타입 검증 실패
 */
@SuppressWarnings("serial")
public class InvalidFileException extends RuntimeException{
    // 기본 생성자 - 메세지만 전달
    public InvalidFileException(String message) {
        super(message);
    }
    // 원인 예외를 포함하는 생성자
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
