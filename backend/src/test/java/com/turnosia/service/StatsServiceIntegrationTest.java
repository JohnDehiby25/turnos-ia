package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;
import com.turnosia.repository.AppointmentRepository;
import com.turnosia.repository.ClientRepository;
import com.turnosia.repository.ServiceCount;
import com.turnosia.repository.ServiceTypeRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class StatsServiceIntegrationTest {

	/** Jueves 19/11/2026 en Bogotá (14:00 UTC = 9:00 a. m.). */
	private static final Instant DAY = Instant.parse("2026-11-19T14:00:00Z");
	private static final Duration SLOT = Duration.ofMinutes(45);

	@Autowired
	private StatsService statsService;

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private ServiceTypeRepository serviceTypeRepository;

	@Test
	void summary_readsMoneyTipsAndTopServicesFromDatabase() {
		Client client = clientRepository.save(new Client("Ana", "Gómez", null));
		ServiceType cepillado = serviceTypeRepository.save(new ServiceType("Cepillado", 45, null));
		ServiceType mechas = serviceTypeRepository.save(new ServiceType("Mechas", 180, null));

		save(client, cepillado, DAY, AppointmentStatus.ATTENDED, "35000.00", "5000.00");
		save(client, mechas, DAY.plus(SLOT), AppointmentStatus.ATTENDED, "120000.00", null);
		save(client, cepillado, DAY.plusSeconds(7200), AppointmentStatus.SCHEDULED, "35000.00", null);
		save(client, cepillado, DAY.plusSeconds(10800), AppointmentStatus.CANCELLED, "35000.00", null);
		// Otro día: no entra en el resumen.
		save(client, cepillado, DAY.plusSeconds(86400L * 2), AppointmentStatus.ATTENDED, "99000.00", null);

		StatsSummary summary = statsService.summary(LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 19));

		assertThat(summary.earned()).isEqualByComparingTo("155000.00");
		assertThat(summary.projected()).isEqualByComparingTo("35000.00");
		assertThat(summary.notCollected()).isEqualByComparingTo("35000.00");
		assertThat(summary.tips()).isEqualByComparingTo("5000.00");
		assertThat(summary.countByStatus().get(AppointmentStatus.ATTENDED)).isEqualTo(2L);
		assertThat(summary.topServices()).containsExactly(
				new ServiceCount("Cepillado", 2L),
				new ServiceCount("Mechas", 1L));
	}

	private void save(Client client, ServiceType service, Instant start, AppointmentStatus status,
			String price, String tip) {
		Appointment appointment = new Appointment(client, service, start, start.plus(SLOT));
		appointment.chargePrice(new BigDecimal(price));
		if (tip != null) {
			appointment.recordTip(new BigDecimal(tip));
		}
		appointment.changeStatus(status, start);
		appointmentRepository.saveAndFlush(appointment);
	}
}
