package com.library.dto;

import java.time.LocalDateTime;

import com.library.entity.Member;
import com.library.entity.MemberStatus;
import com.library.entity.MemberType;
import com.library.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * 회원 정보 응답 dto
 *     - 사용 목적
 *         - 회원 정보 조회 결과 반환
 *         - 민감한 정보(비밀번호, 계좌정보) 제외
 *         - API 응답 표준화
 *     - 변환
 *         - Member Entity => MemberResponseDto
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberResponseDto {
	// 기본 식별 정보
	private Long id;
	private String email;
	private String name;
	private String phone;
	private String address;
	// 회원 가입 정보
	private MemberType type;
	private MemberStatus status;
	private Role role;
	private LocalDateTime joinDateTime;
	private LocalDateTime updatedAt;
	public static MemberResponseDto from(Member member) {
		if (member == null) {
			return null;
		}
		return MemberResponseDto.builder()
								.id(member.getId())
								.email(member.getEmail())
								.address(member.getAddress())
								.name(member.getName())
								.phone(member.getPhone())
								.type(member.getType())
								.status(member.getStatus())
								.role(member.getRole())
								.joinDateTime(member.getJoinDate())
								.updatedAt(member.getJoinDate())
								.build();
	}
}
