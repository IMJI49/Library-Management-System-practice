package com.library.util;

/*
 * 개인정보 마스킹 처리를 위한 유틸리티 클래스
 *       - 이메일 마스킹 : user@domain.com => u***@domain.com
 *       - 이름 마스킹 : 홍길동 => 홍*동
 *       - 전화번호 마스킹 : 010-1234-5678 => 010-****-5678
 *       - 주소 마스킹 : 서울특별시 강남구 역삼동 => 서울특별시 강남구
 */
public class MaskingUtils {
	/** 이메일 마스킹 메소드 */
	public static String maskEmail(String email) {
		// 1. 빈경우
		if (email == null || email.isEmpty()) {
			return "N/A";
		}
		// 2. @ 위치 찾기(이메일 형식 확인)
		int atIndex = email.indexOf("@");
		// 3. 기호가 없거나 맨앞에 있는 경우
		if (atIndex < 1) {
			return email;
		}
		// 4. 이메일을 사용자 명(@앞)과 도메인(@뒤)
		String username = email.substring(0, atIndex);	// @ 앞부분 추출
		String domain = email.substring(atIndex);		// @ 포함 뒷부분 추출
		// 5. 사용자명 길이에 따른 마스킹 처리
		if (username.length() <= 2) {
			// 첫글자 + * + 도메인
			return username.charAt(0)+"*"+domain;
		} else {
			return username.charAt(0)+"***"+domain;
		}
		
	}
	
}
