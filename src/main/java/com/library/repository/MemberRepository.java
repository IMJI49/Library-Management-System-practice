package com.library.repository;

import com.library.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * 회원 데이터 접근 리포짓토리
 *     - Member 엔티티의 DB CRUD 연산 기능 제공
 *     - 기본 CRUD : save(), findById(), findAll() 등
 *     - JPA Query Method : findByEmail(), existsByEmail() 등 
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
	// 이메일로 회원 단건 조회 - 로그인 인증 시 사용자 정보 조회
	Optional<Member> findByEmail(String email);
	// 이메일 존재 여부 확인 (중복 체크) - 회원 가입시 이메일 중복 체크
	boolean existsByEmail(String email);
}
