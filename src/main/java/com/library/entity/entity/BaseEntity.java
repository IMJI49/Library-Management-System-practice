package com.library.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
    모든 Entity의 공통 필드를 관리하는 추상 클래스
        - 생성 일시와 수정 일시를 자동으로 관리함.
        - JPA Auditing 기능을 통해 Entity 생성/수정 시 자동으로 시간이 기록됨.
 */
@Getter
@MappedSuperclass   // JPA Entity 클래스들이 이 클래스를 상속할 경우 필드를 컬럼으로 인식
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능 활성화
public abstract class BaseEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
