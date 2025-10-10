package com.library.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.MemberRegistrationDto;
import com.library.dto.MemberResponseDto;
import com.library.entity.Member;
import com.library.entity.MemberStatus;
import com.library.entity.MemberType;
import com.library.entity.Role;
import com.library.repository.MemberRepository;
import com.library.util.MaskingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 서비스 레이어 주요 책임 : 
 *  - 회원 조회/수정/가입/탈퇴 비지니스 로직
 *  - 비밀번호 암호화 보안 처리
 *     - Bcrypt 기반 비밀번호 암호화
 *  - 회원 상태 및 등급 관리
 *  - 이메일 중복 체크 등 유효성 검증
 *  - 보안 기능
 *       - 중복 가입 방지
 *       - 개인 정보 처리 시 로깅 마스킹
 *       - 트랜젝션 기반 데이터 무결성 보장
 *  회원 가입 처리 단계 : 
 *        - 1) 이메일 중복 체크 (유니크 제약 검증)
 *        - 2) 비밀번호 Bcrypt 암호화
 *        - 3) 기본값 자동 설정 (등급, 상태, 권한)
 *        - 4) 가입일시 자동 기록
 *        - 5) 데이터 베이스 저장
 *        - 6) 가입 완료 로깅 및 통계 업데이트
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // 읽기 전용 트랙잭션 (성능 최적화)
public class MemberService {
	private final MemberRepository memberRepository;
//	private final BCryptPasswordEncoder encoder;
	private final PasswordEncoder encoder;
	@Transactional
	public MemberResponseDto register(MemberRegistrationDto dto) {
		String maskedEmail = MaskingUtils.maskEmail(dto.getEmail());
		log.info("=== 👤 회원가입 비즈니스 로직 시작 : {} ===", maskedEmail);
		try {
			// 1. DTO 유효성 검사
			log.info("DTO 유효성 검사 진행 중");
			// 비밀번호 확인 검증
			if (dto.isPasswordMatching()) {
				log.error("❌ 비밀번호 불일치 감지 : {}", maskedEmail);
				log.error("\t|__ 비밀번호와 확인 비밀번호가 다름");
				throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
			}
			// 약관 동의 확인
			if (!dto.isAllTermsAgreed()) {
				log.error("❌ 약관 동의 누락 : {}", maskedEmail);
				log.error("\t|__ 필수 약관에 동의하지 않음");
				throw new IllegalArgumentException("필수 약관에 동의해야 합니다.");
			}
			log.info("DTO 유효성 검사 통과");
			// 2. 이메일 중복 체크(필수 검증)
			if (memberRepository.existsByEmail(dto.getEmail())) {
				log.error("❌ 이메일 중복 감지 : {}", maskedEmail);
				log.error("\t|__ 기존 회원과 동일한 이메일로 가입 시도");
				throw new IllegalArgumentException("이미 사용중인 이메일 입니다." + dto.getEmail());
			}
			log.info("이메일 중복 확인 통과");
			log.info("\t|____새로운 이메일 확인 : {}",maskedEmail);
			// === 3. DTO -> Entity 변환 ===
			log.info("DTO에서 Entity 변환 중 ...");
			Member member = Member.builder()
							.email(dto.getEmail())
							.password(dto.getPassword())	// 밑에서 암호화
							.name(dto.getName())
							.phone(dto.getPhone())
							.address(dto.getAddress())
							.build();
			log.info("Entity 변환 완료");
			// === 4. 비밀번호 암호화(보안 핵심) ===
			log.info("비밀번호 암호화 처리중...");
			String originalPassword = dto.getPassword();
			if (originalPassword == null || originalPassword.trim().isEmpty()) {
				log.error("❌ 비밀번호가 비어있음");
				throw new IllegalArgumentException("비밀번호가 비어있습니다.");
			}
			String encodedPassword = encoder.encode(originalPassword);
			member.setPassword(encodedPassword);
			log.info("✅ 비밀번호 암호화 완료");
			log.info("\t|- 알고리즘 : Bcrypt");
			log.info("\t|- 원본 길이 : {}자",originalPassword.length());
			log.info("\t|- 암호화 결과 길이 : {}자",encodedPassword.length());
			// === 5. 기본값 자동 설정 (비지니스 규칙) ===
			// 가입일시 설정(시스템 시간 기준)
			if (member.getJoinDate() == null) {
				LocalDateTime joinDateTime = LocalDateTime.now();
				member.setJoinDate(joinDateTime);
				log.info("\t|-가입일시 : {}",joinDateTime);
			}
			if (member.getType() == null) {
				member.setType(MemberType.REGULAR);
				log.info("\t|-회원등급 : {}({}권 대출 가능)",MemberType.REGULAR.getDescription(),MemberType.REGULAR.getMaxRentalBooks());
			}
			// 계정 상태 설정(신규 회원은 활성 상태)
			if (member.getStatus() == null) {
				member.setStatus(MemberStatus.ACTIVE);
				log.info("\t|-계정 상태 : {}(로그인 가능)",MemberStatus.ACTIVE);
			}
			if (member.getRole() == null) {
				member.setRole(Role.USER);
				log.info("\t|-시스템 권한 : {}({})",Role.USER.getDescription(),Role.USER.getKey());
			}
			log.info("✅ 기본값 설정 완료");
			// === 6. 데이터베이스 저장(영속화)===
			log.info("데이터베이스 저장 중...");
			Member savedMember = memberRepository.save(member);
			// === 7. 저장 결과 검증 ===
			if (savedMember.getId() == null) {
				log.error("❌ 회원 저장 실패 - ID가 할당되지 않음");
				throw new RuntimeException("회원 정보 저장에 실패했습니다.");
			}
			// 추가적인 로그 추가 나중에...
			log.info("=== ✅ 회원가입 비지니스 로직 완료 ===");
			log.info("Entity에서 ResponseDto로 변환 중....");
			
			return MemberResponseDto.from(savedMember);
		} catch (IllegalArgumentException e) {
			// 비지니스 규칙 위반(이메일 중복 등)
			log.error("회원 가입 비지니스 규칙 위반 : {}",e.getMessage());
			throw e;
		} catch (Exception e) {
			// 시스템 오류(DB 연결 실패, 암호화 오류등)@
			log.error("회원강비 시스템 오류 발생!");
			throw new RuntimeException("회원가입 중 시스템 오류가 발생했습니다.");
		}
		
	}
	public boolean existsByEmail(String email) {
		String maskedEmil = MaskingUtils.maskEmail(email);
		log.info("이메일 존재 여부 확인 : {}",maskedEmil);
		try {
			boolean exists = memberRepository.existsByEmail(email);
			log.info("이메일 중복체크 결과 : {} -> {}",maskedEmil,exists? "이미사용중" : "사용 가능");
			if (exists) {
				log.info("\t|__기존 회원의 이메일과 일치");
			} else {
				log.info("\t|__새로운 이메일 확인함");
			}
			return exists;
		} catch (Exception e) {
			log.error("이메일 존재 여부 확인 중 오류 : {}", maskedEmil);
			log.error("\t|__안전을 위해 중복으로 판정");
			return true;
		}
		
	}
	

}
