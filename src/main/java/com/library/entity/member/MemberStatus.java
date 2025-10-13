package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter@AllArgsConstructor
public enum MemberStatus {
	ACTIVE("활성"), SUSPENDED("정지"), WITHDRAWN("탈퇴");
	private final String description;
	public boolean canTransitionTo(MemberStatus targetStatus) {
        return switch (this) {
            case ACTIVE ->
                // 활성 -> 정지/탈퇴 가능
                    targetStatus == SUSPENDED || targetStatus == WITHDRAWN;
            case SUSPENDED -> targetStatus == ACTIVE || targetStatus == WITHDRAWN;
            default -> false;
        };
	}

}
