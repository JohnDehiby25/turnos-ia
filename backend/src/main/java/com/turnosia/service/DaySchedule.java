package com.turnosia.service;

import java.time.LocalDate;
import java.time.LocalTime;

public record DaySchedule(LocalDate date, boolean open, LocalTime openTime, LocalTime closeTime,
		LocalTime lunchStart, LocalTime lunchEnd) {

	public static DaySchedule closed(LocalDate date) {
		return new DaySchedule(date, false, null, null, null, null);
	}
}
