package com.turnosia.service;

import java.time.ZoneId;

/** Zona horaria del negocio: días, semanas y horarios se leen en America/Bogota. */
final class BusinessTime {

	static final ZoneId ZONE = ZoneId.of("America/Bogota");

	private BusinessTime() {
	}
}
