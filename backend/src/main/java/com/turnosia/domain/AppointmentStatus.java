package com.turnosia.domain;

/**
 * Estados de una cita. Se guardan como texto en la BD (CHECK en V1__init.sql).
 */
public enum AppointmentStatus {
	SCHEDULED,
	ATTENDED,
	CANCELLED,
	NO_SHOW;

	/** Solo una cita por atender cambia de estado; los estados finales no vuelven atrás. */
	public boolean canTransitionTo(AppointmentStatus next) {
		return this == SCHEDULED && next != SCHEDULED;
	}
}
