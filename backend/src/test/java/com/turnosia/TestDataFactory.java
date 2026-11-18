package com.turnosia;

import java.time.OffsetDateTime;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;

/**
 * Fábrica pequeña de datos de prueba (convención de TESTING.md).
 */
public final class TestDataFactory {

	private TestDataFactory() {
	}

	public static Client client(String name, String phone) {
		return new Client(name, phone);
	}

	public static ServiceType serviceType(String name, int durationMin) {
		return new ServiceType(name, durationMin, null);
	}

	public static Appointment appointment(Client client, ServiceType serviceType,
			OffsetDateTime start, OffsetDateTime end) {
		return new Appointment(client, serviceType, start.toInstant(), end.toInstant());
	}
}
