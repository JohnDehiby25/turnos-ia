package com.turnosia.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Excepción por fecha (ej. domingos que sí se trabajan, días sin almuerzo).
 * lunch NULL = sin almuerzo ese día.
 */
@Entity
@Table(name = "day_override")
public class DayOverride {

	@Id
	private LocalDate date;

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

	@Column(length = 255)
	private String note;

	protected DayOverride() {
	}

	public DayOverride(LocalDate date, boolean open, LocalTime openTime, LocalTime closeTime,
			LocalTime lunchStart, LocalTime lunchEnd, String note) {
		this.date = date;
		this.open = open;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.lunchStart = lunchStart;
		this.lunchEnd = lunchEnd;
		this.note = note;
	}

	public LocalDate getDate() {
		return date;
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

	public String getNote() {
		return note;
	}
}
