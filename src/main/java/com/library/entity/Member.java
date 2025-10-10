package com.library.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 *  Spring Security 연동
 *     - User Details 인터페이스 구현
 *     - 권한 기반 접근 제어 (ROLE_ ...)
 *     - 계정 상태별 로그인 제어
 */
@SuppressWarnings("serial")
@Entity @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "members", uniqueConstraints = {
		@UniqueConstraint(columnNames = "email", name = "UK_USERNAME_MEMBERS") })
public class Member implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;
	@Column(nullable = false, unique = true, length = 100)
	private String email;// 이메일 (로그인 ID로 사용)
	@Column(nullable = false)
	private String password;
	@Column(nullable = false, length = 50)
	private String name;
	@Column(length = 20)
	private String phone;
	@Column(length = 200)
	private String address;
	@Column(name = "join_date", updatable = false)
	private LocalDateTime joinDate;
	@Enumerated(EnumType.STRING)
	@Builder.Default
	@Column(length = 20)
	private Role role = Role.USER;
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	@Builder.Default
	private MemberStatus status = MemberStatus.ACTIVE;
	@Enumerated(EnumType.STRING) // Enum 타입을 DB에 저장하는 저장하는 방법을 지정하는 어노테이션 => "문자열로 지정"
	@Builder.Default
	@Column(name = "member_type", length = 20)
	private MemberType type = MemberType.REGULAR;
	
	// 감사 로그(시스템 추적 정보)
	@Column(updatable = false)
	private LocalDateTime createdAt; // 회원 가입 완료 시점 (immutable, 데이터 감사 추적용)
	@Column
	private LocalDateTime updatedAt;	// 회원 정보 변경 시마다 자동 업데이트(변경 이력 추적용)
	/*
	 *    엔티티 최초 저장 전 실행되는 콜백
	 *       자동 설정 항목
	 *       - joinDate : 가입일 = 현재 시간
	 *       - createdAt : 생성일 = 현재 시간
	 *       - updatedAt : 수정이 = 현재 시간
	 *       
	 *       * memberRepository.save() 최초 실행 시
	 *         em.persist()호출 시 
	 */
	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		joinDate = now;
		createdAt = now;
		updatedAt = now;
    }
	/*
	 * 엔티티 수정 전 실행되는 콜백
	 *     * 엔티티 필드 변경 후 트랜젝션 커밋 시
	 */
	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
	/*
	 *  Spring Security 권한 목록 반환
	 *        - 권한 
	 *          - Role enum(...)
	 *          - Spring Security Authority (ROLE_...)
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		// enum형 Role.USER -> Role_...
		authorities.add(new SimpleGrantedAuthority("ROLE_"+role.name()));
		return authorities;
	}
    /*
        Spring Security 인증용 비밀번호 반환
            - 로그인 시 : PasswordEncoder가 입력된 비밀번호와 DB의 암호화된 비밀번호를 비교
     */
	@Override
	public String getPassword() {

		return password;
	}

	@Override
	public String getUsername() {

		return email;
	}

}
