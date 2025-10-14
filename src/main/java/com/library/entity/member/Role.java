package com.library.entity.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * 사용자 권한을 정의하는 열거형
 * Spring Security의 권한 체계와 연동
 */
@Getter@AllArgsConstructor
public enum Role {
	USER("Role_USER","일반 사용자"),
	LIBRARIAN("ROLE_LIBRARIAN","사서"),
	ADMIN("ROLE_ADMIN","관리자");
	private final String key;
	private final String description;
	// 권한 레벨 반환
	public int getLevel() {
        return switch (this) {
            case USER -> 1;
            case LIBRARIAN -> 2;
            case ADMIN -> 3;
        };
	}
	
}
