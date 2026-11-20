package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;
import com.turnosia.domain.WorkingHours;
import com.turnosia.repository.AppointmentRepository;
import com.turnosia.repository.ClientRepository;
import com.turnosia.repository.ServiceTypeRepository;
import com.turnosia.repository.WorkingHoursRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AppointmentServiceIntegrationTest {

	private static final Instant MONDAY_9 = Instant.parse("2030-01-07T14:00:00Z");
	private static final Duration CEPILLADO = Duration.ofMinutes(45);

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private ServiceTypeRepository serviceTypeRepository;

	@Autowired
	private WorkingHoursRepository workingHoursRepository;

	@Autowired
	private JdbcTemplate jdbc;

	private Long cepilladoId;

	@BeforeEach
	void setUp() {
		workingHoursRepository.save(new WorkingHours(DayOfWeek.MONDAY, true,
				LocalTime.of(8, 0), LocalTime.of(18, 0), LocalTime.of(12, 0), LocalTime.of(13, 0)));
		cepilladoId = serviceTypeRepository.save(new ServiceType("Cepillado", 45, new BigDecimal("35000.00"))).getId();
	}

	private static Appointment appointmentAt(Client client, ServiceType service, Instant start) {
		Appointment appointment = new Appointment(client, service, start, start.plus(CEPILLADO));
		appointment.chargePrice(new BigDecimal("35000.00"));
		return appointment;
	}

	@Test
	void create_freeSlot_usesServiceDurationAndPersists() {
		Appointment appointment = appointmentService.create("Ana", "Gómez", "3001234567", cepilladoId, MONDAY_9, null);

		assertThat(appointment.getId()).isNotNull();
		assertThat(appointment.getEndAt()).isEqualTo(MONDAY_9.plus(CEPILLADO));
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
	}

	@Test
	void create_overlappingSlot_throwsNotAvailable() {
		appointmentService.create("Ana", "Gómez", null, cepilladoId, MONDAY_9, null);

		assertThatThrownBy(() -> appointmentService.create("Luis", "Pérez", null, cepilladoId,
				MONDAY_9.plusSeconds(1800), null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_NOT_AVAILABLE"));
	}

	@Test
	void cancel_thenSameSlotIsAvailableAgain() {
		Appointment first = appointmentService.create("Ana", "Gómez", null, cepilladoId, MONDAY_9, null);
		appointmentService.cancel(first.getId());

		Appointment second = appointmentService.create("Luis", "Pérez", null, cepilladoId, MONDAY_9, null);

		assertThat(second.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
	}

	@Test
	void reschedule_toSlotOverlappingItsOwnOldSlot_isAllowed() {
		Appointment appointment = appointmentService.create("Ana", "Gómez", null, cepilladoId, MONDAY_9, null);
		Instant newStart = MONDAY_9.plusSeconds(900);

		Appointment moved = appointmentService.reschedule(appointment.getId(), newStart, null);

		assertThat(moved.getStartAt()).isEqualTo(newStart);
		assertThat(moved.getEndAt()).isEqualTo(newStart.plus(CEPILLADO));
	}

	@Test
	void create_inactiveService_throwsServiceNotFound() {
		jdbc.update("INSERT INTO service_type (name, duration_min, price, active) VALUES ('Inactivo', 30, NULL, FALSE)");
		Long inactiveId = jdbc.queryForObject("SELECT id FROM service_type WHERE name = 'Inactivo'", Long.class);

		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, inactiveId, MONDAY_9, null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("SERVICE_NOT_FOUND"));
	}

	@Test
	void databaseRejectsOverlap_errorMentionsConstraintName() {
		Client client = clientRepository.save(new Client("Ana", "Gómez", null));
		ServiceType service = serviceTypeRepository.findById(cepilladoId).orElseThrow();
		appointmentRepository.saveAndFlush(appointmentAt(client, service, MONDAY_9));
		Appointment overlapping = appointmentAt(client, service, MONDAY_9.plusSeconds(1800));

		assertThatThrownBy(() -> appointmentRepository.saveAndFlush(overlapping))
				.isInstanceOfSatisfying(DataIntegrityViolationException.class,
						e -> assertThat(e.getMostSpecificCause().getMessage()).contains("appointment_no_overlap"));
	}
}
