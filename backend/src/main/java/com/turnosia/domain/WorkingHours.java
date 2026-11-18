package com.turnosia.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Horario base semanal. El almuerzo es opcional por día (días muy agendados:
 * sin almuerzo).
 */
@Entity
@Table(name = "working_hours")
public class WorkingHours {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Convert(converter = DayOfWeekConverter.class)
	@Column(nullable = false, unique = true)
	private DayOfWeek weekday;

	@Column(name = "is_open", nullable = false)
	private boolean open;

	@Column(name = "open_time")
	private LocalTime openTime;

	@Column(name = "close_time")
	private LocalTime closeTime;

	@Column(name = "lunch_start")
	private LocalTime lunchStart;

	@Column(name = "lunch_end")
	private LocalTime lunchEnd;

	protected WorkingHours() {
	}

	public WorkingHours(DayOfWeek weekday, boolean open, LocalTime openTime, LocalTime closeTime,
			LocalTime lunchStart, LocalTime lunchEnd) {
		this.weekday = weekday;
		this.open = open;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.lunchStart = lunchStart;
		this.lunchEnd = lunchEnd;
	}

	public Long getId() {
		return id;
	}

	public DayOfWeek getWeekday() {
		return weekday;
	}

	public boolean isOpen() {
		return open;
	}

	public LocalTime getOpenTime() {
		return openTime;
	}

	public LocalTime getCloseTime() {
		return closeTime;
	}

	public LocalTime getLunchStart() {
		return lunchStart;
	}

	public LocalTime getLunchEnd() {
		return lunchEnd;
	}
}
