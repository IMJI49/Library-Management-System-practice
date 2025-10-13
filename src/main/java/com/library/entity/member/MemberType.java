package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberType {
	REGULAR("일반회원", 3, 14, 1, 1), SILVER("실버회원", 5, 21, 2, 2), GOLD("골드회원", 7, 21, 2, 3),
	VIP("VIP 회원", 10, 30, 5, Integer.MAX_VALUE);

	private final String description;
	private final int maxRentalBooks;
	private final int rentalPeriodDays;
	private final int maxReservationBooks;
	private final int maxRenewalCount;
	// 등급 레벨 반환
	public int getLevel() {
        return switch (this) {
            case REGULAR -> 1;
            case SILVER -> 2;
            case GOLD -> 3;
            case VIP -> 4;
        };
	}

}
