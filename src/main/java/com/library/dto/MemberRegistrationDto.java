package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * 회원 가입 전용 Dto
 *       - 사용 목적
 *            - 회원 가입시에만 사용되는 보안 강화 DTO
 *            - 불필요한 필드 노출 차단
 *            - 유효성 검사 규칙 적용
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MemberRegistrationDto {
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 100, message = "이메일은 100자를 초과 할 수 없습니다.")
	private String email;
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Size(min = 8,max = 20,message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@$%&?*]{8,20}$", message = "비밀번호는 영문, 숫자를 포함해야 합니다.")
	private String password;
	@NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
	private String confirmPassword;
	@NotBlank(message = "이름은 필수 입력값입니다.")
	@Size(max = 50,min = 2,message = "이름은 2자이상 50자 이하여야 합니다.")
	private String name;
	// 전화번호 형식 : 010-1234-5678
	@Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$",message = "전화번호 형식이 올바르지 않습니다.(예: 010-1234-5678)")
	private String phone;
	@Size(max = 200,message = "주소는 200자를 초과할 수 없습니다.")
	private String address;
	@AssertTrue(message = "서비스 이용약관에 동의해야 합니다.")
	private boolean termsAgreed;
	@AssertTrue(message = "개인정보 처리방침에 동의해야합니다.")
	private boolean privacyAgreed;
	public boolean isPasswordMatching() {
		return password != null && password.equals(confirmPassword);
	}
	// 모든 필수 약관에 동의 여부 확인
	public boolean isAllTermsAgreed() {
		return termsAgreed && privacyAgreed;
	}
}
