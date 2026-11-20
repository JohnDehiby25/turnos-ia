package com.turnosia;

import java.math.BigDecimal;
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

	public static Client client(String firstName, String lastName, String phone) {
		return new Client(firstName, lastName, phone);
	}

	public static ServiceType serviceType(String name, int durationMin) {
		return new ServiceType(name, durationMin, null);
	}

	public static Appointment appointment(Client client, ServiceType serviceType,
			OffsetDateTime start, OffsetDateTime end) {
		Appointment appointment = new Appointment(client, serviceType, start.toInstant(), end.toInstant());
		appointment.chargePrice(new BigDecimal("35000.00"));
		return appointment;
	}
}
