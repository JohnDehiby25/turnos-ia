package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.turnosia.domain.Appointment;
import com.turnosia.domain.AppointmentStatus;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;
import com.turnosia.repository.AppointmentRepository;
import com.turnosia.repository.ServiceTypeRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

	private static final Instant NOW = Instant.parse("2026-11-18T14:00:00Z");
	private static final Instant START = Instant.parse("2026-11-19T14:00:00Z");
	private static final Long SERVICE_ID = 1L;
	private static final Long APPOINTMENT_ID = 10L;

	@Mock
	private ClientService clientService;

	@Mock
	private ServiceTypeRepository serviceTypeRepository;

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private AvailabilityService availabilityService;

	private AppointmentService appointmentService;
	private ServiceType cepillado;
	private Client ana;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
		appointmentService = new AppointmentService(clientService, serviceTypeRepository, appointmentRepository,
				availabilityService, clock);
		cepillado = new ServiceType("Cepillado", 45, new BigDecimal("35000.00"));
		ana = new Client("Ana", "Gómez", null);
	}

	@Test
	void create_withoutEnd_usesServiceDuration() {
		givenService();
		when(availabilityService.isAvailable(START, START.plus(Duration.ofMinutes(45)), null)).thenReturn(true);
		when(clientService.resolveClient("Ana", "Gómez", null)).thenReturn(ana);
		givenSaveReturnsArgument();

		Appointment appointment = appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null);

		assertThat(appointment.getEndAt()).isEqualTo(START.plus(Duration.ofMinutes(45)));
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
		assertThat(appointment.getClient()).isSameAs(ana);
	}

	@Test
	void create_withExplicitEnd_usesGivenEnd() {
		Instant end = START.plus(Duration.ofMinutes(60));
		givenService();
		when(availabilityService.isAvailable(START, end, null)).thenReturn(true);
		when(clientService.resolveClient("Ana", "Gómez", null)).thenReturn(ana);
		givenSaveReturnsArgument();

		Appointment appointment = appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, end);

		assertThat(appointment.getEndAt()).isEqualTo(end);
	}

	@Test
	void create_withoutPrice_copiesServicePrice() {
		cepillado.changePrice(new BigDecimal("35000.00"));
		givenService();
		when(availabilityService.isAvailable(START, START.plus(Duration.ofMinutes(45)), null)).thenReturn(true);
		when(clientService.resolveClient("Ana", "Gómez", null)).thenReturn(ana);
		givenSaveReturnsArgument();

		Appointment appointment = appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null);

		assertThat(appointment.getPriceCharged()).isEqualByComparingTo("35000.00");
	}

	@Test
	void create_withCasePrice_usesGivenPriceInsteadOfServicePrice() {
		cepillado.changePrice(new BigDecimal("35000.00"));
		givenService();
		when(availabilityService.isAvailable(START, START.plus(Duration.ofMinutes(45)), null)).thenReturn(true);
		when(clientService.resolveClient("Ana", "Gómez", null)).thenReturn(ana);
		givenSaveReturnsArgument();

		Appointment appointment = appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null,
				new BigDecimal("20000.00"));

		assertThat(appointment.getPriceCharged()).isEqualByComparingTo("20000.00");
	}

	@Test
	void create_withoutAnyPrice_throwsPriceRequired() {
		cepillado.changePrice(null);
		givenService();

		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_PRICE_REQUIRED"));
	}

	@Test
	void create_afterServicePriceChanges_keepsPriceOfExistingAppointment() {
		cepillado.changePrice(new BigDecimal("35000.00"));
		givenService();
		when(availabilityService.isAvailable(START, START.plus(Duration.ofMinutes(45)), null)).thenReturn(true);
		when(clientService.resolveClient("Ana", "Gómez", null)).thenReturn(ana);
		givenSaveReturnsArgument();
		Appointment appointment = appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null);

		cepillado.changePrice(new BigDecimal("50000.00"));

		assertThat(appointment.getPriceCharged()).isEqualByComparingTo("35000.00");
	}

	@Test
	void create_startInThePast_throwsInPast() {
		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, SERVICE_ID,
				NOW.minusSeconds(60), null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_IN_PAST"));
	}

	@Test
	void create_missingStart_throwsStartRequired() {
		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, SERVICE_ID, null, null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_START_REQUIRED"));
	}

	@Test
	void create_endNotAfterStart_throwsInvalidRange() {
		givenService();

		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, START))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_INVALID_RANGE"));
	}

	@Test
	void create_unknownService_throwsServiceNotFound() {
		when(serviceTypeRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("SERVICE_NOT_FOUND"));
	}

	@Test
	void create_slotNotAvailable_throwsNotAvailableWithoutCreatingClient() {
		givenService();
		when(availabilityService.isAvailable(START, START.plus(Duration.ofMinutes(45)), null)).thenReturn(false);

		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_NOT_AVAILABLE"));
		verify(clientService, never()).resolveClient(any(), any(), any());
	}

	@Test
	void create_databaseRejectsOverlap_throwsNotAvailable() {
		givenService();
		when(availabilityService.isAvailable(START, START.plus(Duration.ofMinutes(45)), null)).thenReturn(true);
		when(clientService.resolveClient("Ana", "Gómez", null)).thenReturn(ana);
		when(appointmentRepository.saveAndFlush(any(Appointment.class))).thenThrow(new DataIntegrityViolationException(
				"conflict", new RuntimeException("violates exclusion constraint \"appointment_no_overlap\"")));

		assertThatThrownBy(() -> appointmentService.create("Ana", "Gómez", null, SERVICE_ID, START, null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_NOT_AVAILABLE"));
	}

	@Test
	void reschedule_scheduled_updatesTimesAndUpdatedAtButKeepsCreatedAt() {
		Appointment existing = new Appointment(ana, cepillado, START, START.plus(Duration.ofMinutes(45)));
		Instant createdAt = existing.getCreatedAt();
		Instant newStart = START.plus(Duration.ofHours(1));
		Instant newEnd = newStart.plus(Duration.ofMinutes(45));
		givenAppointment(existing);
		when(availabilityService.isAvailable(newStart, newEnd, APPOINTMENT_ID)).thenReturn(true);
		givenSaveReturnsArgument();

		Appointment result = appointmentService.reschedule(APPOINTMENT_ID, newStart, null);

		assertThat(result.getStartAt()).isEqualTo(newStart);
		assertThat(result.getEndAt()).isEqualTo(newEnd);
		assertThat(result.getUpdatedAt()).isEqualTo(NOW);
		assertThat(result.getCreatedAt()).isEqualTo(createdAt);
	}

	@Test
	void reschedule_cancelledAppointment_throwsNotReschedulable() {
		Appointment existing = new Appointment(ana, cepillado, START, START.plus(Duration.ofMinutes(45)));
		existing.changeStatus(AppointmentStatus.CANCELLED, NOW);
		givenAppointment(existing);

		assertThatThrownBy(() -> appointmentService.reschedule(APPOINTMENT_ID, START.plusSeconds(3600), null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_NOT_RESCHEDULABLE"));
	}

	@Test
	void reschedule_newSlotNotAvailable_throwsNotAvailable() {
		Appointment existing = new Appointment(ana, cepillado, START, START.plus(Duration.ofMinutes(45)));
		givenAppointment(existing);
		when(availabilityService.isAvailable(any(), any(), any())).thenReturn(false);

		assertThatThrownBy(() -> appointmentService.reschedule(APPOINTMENT_ID, START.plusSeconds(3600), null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_NOT_AVAILABLE"));
	}

	@Test
	void cancel_scheduled_setsCancelledAndUpdatedAt() {
		Appointment existing = new Appointment(ana, cepillado, START, START.plus(Duration.ofMinutes(45)));
		givenAppointment(existing);

		Appointment result = appointmentService.cancel(APPOINTMENT_ID);

		assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
		assertThat(result.getUpdatedAt()).isEqualTo(NOW);
	}

	@Test
	void markAttended_scheduled_setsAttended() {
		givenAppointment(new Appointment(ana, cepillado, START, START.plus(Duration.ofMinutes(45))));

		assertThat(appointmentService.markAttended(APPOINTMENT_ID).getStatus()).isEqualTo(AppointmentStatus.ATTENDED);
	}

	@Test
	void markNoShow_scheduled_setsNoShow() {
		givenAppointment(new Appointment(ana, cepillado, START, START.plus(Duration.ofMinutes(45))));

		assertThat(appointmentService.markNoShow(APPOINTMENT_ID).getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
	}

	@Test
	void cancel_alreadyAttended_throwsInvalidTransition() {
		Appointment existing = new Appointment(ana, cepillado, START, START.plus(Duration.ofMinutes(45)));
		existing.changeStatus(AppointmentStatus.ATTENDED, NOW);
		givenAppointment(existing);

		assertThatThrownBy(() -> appointmentService.cancel(APPOINTMENT_ID))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("INVALID_STATUS_TRANSITION"));
	}

	@Test
	void cancel_unknownAppointment_throwsNotFound() {
		when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> appointmentService.cancel(APPOINTMENT_ID))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_NOT_FOUND"));
	}

	@Test
	void listBetween_convertsBogotaDaysToUtcRange() {
		when(appointmentRepository.findStartingBetween(any(), any(), any())).thenReturn(List.of());

		appointmentService.listBetween(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18), null);

		verify(appointmentRepository).findStartingBetween(
				Instant.parse("2026-11-18T05:00:00Z"), Instant.parse("2026-11-19T05:00:00Z"), null);
	}

	@Test
	void listBetween_reversedRange_throwsInvalidRange() {
		assertThatThrownBy(() -> appointmentService.listBetween(LocalDate.of(2026, 11, 19),
				LocalDate.of(2026, 11, 18), null))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("APPOINTMENT_INVALID_RANGE"));
	}

	private void givenService() {
		when(serviceTypeRepository.findById(SERVICE_ID)).thenReturn(Optional.of(cepillado));
	}

	private void givenAppointment(Appointment appointment) {
		when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
	}

	private void givenSaveReturnsArgument() {
		when(appointmentRepository.saveAndFlush(any(Appointment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}
}
