package com.library.dto.member;

import com.library.entity.member.MemberStatus;
import com.library.entity.member.MemberType;
import com.library.entity.member.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
 * Data Transfer Object
 *        - 클라이언트와 서버간 데이터 전송용
 *        - Entity와 분리하여 필요한 정보만 노출
 *        - 유효성 검증 어노테이션 포함
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemberDto {
	private Long id;
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	private String email;
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Size(min = 8,max = 20,message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
	@Pattern(regexp = "", message = "비밀번호는 영문, 숫자를 포함해야 합니다.")
	private String password;
	private String confirmPassword;
	@NotBlank(message = "이름은 필수 입력값입니다.")
	@Size(max = 50,min = 2,message = "이름은 2자이상 50자 이하여야 합니다.")
	private String name;
	// 전화번호 형식 : 010-1234-5678
	@Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$",message = "전화번호 형식이 올바르지 않습니다.(예: 010-1234-5678)")
	private String phone;
	@Size(max = 200,message = "주소는 200자를 초과할 수 없습니다.")
	private String address;
	// 시스템 정보(읽기 전용)
	private LocalDateTime joinDate;
	private MemberType type;
	private MemberStatus status;
	private Role role;
	// 비밀번호 일치 여부 확인
	public boolean isPasswordMatching() {
		return password != null && password.equals(confirmPassword);
	}
}
