package com.carpool.carpool.enums.reservation;

/**
 * Esta clase contiene los estados que puede poseer una Reservation
 */
public enum ReservationStateEnum {
	PENDING,
	ACCEPTED,
	IN_PROGRESS,
	REJECTED, 
	CANCELLED, 
	COMPLETED, 
	UNPAID, 
	EXPIRED;
}
