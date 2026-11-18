package com.turnosia.domain;

import java.time.DayOfWeek;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link DayOfWeek} a SMALLINT (1=Lunes..7=Domingo), como define la
 * columna `working_hours.weekday` en la migración V1.
 */
@Converter
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Short> {

	@Override
	public Short convertToDatabaseColumn(DayOfWeek weekday) {
		return weekday == null ? null : (short) weekday.getValue();
	}

	@Override
	public DayOfWeek convertToEntityAttribute(Short value) {
		return value == null ? null : DayOfWeek.of(value);
	}
}
