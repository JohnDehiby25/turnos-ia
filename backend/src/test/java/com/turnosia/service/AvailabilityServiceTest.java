package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.turnosia.TestDataFactory;
import com.turnosia.domain.Appointment;
import com.turnosia.domain.Client;
import com.turnosia.domain.ServiceType;
import com.turnosia.repository.AppointmentRepository;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

	private static final LocalDate WEDNESDAY = LocalDate.of(2026, 11, 18);
	private static final LocalDate THURSDAY = LocalDate.of(2026, 11, 19);
	private static final Instant BEFORE_THE_DAY = Instant.parse("2026-11-17T00:00:00Z");

	@Mock
	private ScheduleService scheduleService;

	@Mock
	private AppointmentRepository appointmentRepository;

	private AvailabilityService availabilityService;

	@BeforeEach
	void setUp() {
		availabilityService = serviceAt(BEFORE_THE_DAY);
	}

	@Test
	void freeSlots_emptyDay_returnsStartsEvery15MinutesThatFitWorkingHours() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(10, 0));

		List<FreeSlot> slots = availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(45));

		assertThat(slots).hasSize(6);
		assertThat(slots.get(0).start().toLocalTime()).isEqualTo(LocalTime.of(8, 0));
		assertThat(slots.get(5).start().toLocalTime()).isEqualTo(LocalTime.of(9, 15));
		assertThat(slots.get(5).end().toLocalTime()).isEqualTo(LocalTime.of(10, 0));
	}

	@Test
	void freeSlots_appointmentStartingInsideSlot_excludesCollidingStartsButAllowsAdjacentOnes() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
		givenAppointments(busy("2026-11-18T09:00-05:00", "2026-11-18T10:00-05:00"));

		List<LocalTime> starts = startsOf(availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(60)));

		assertThat(starts).contains(LocalTime.of(8, 0), LocalTime.of(10, 0));
		assertThat(starts).doesNotContain(LocalTime.of(8, 15), LocalTime.of(9, 0), LocalTime.of(9, 45));
	}

	@Test
	void freeSlots_partialOverlapAtStart_excludesStartsThatWouldRunIntoAppointment() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
		givenAppointments(busy("2026-11-18T09:30-05:00", "2026-11-18T10:30-05:00"));

		List<LocalTime> starts = startsOf(availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(60)));

		assertThat(starts).contains(LocalTime.of(8, 30));
		assertThat(starts).doesNotContain(LocalTime.of(8, 45));
	}

	@Test
	void freeSlots_lunchBreak_blocksStartsThatWouldOverlapIt() {
		when(scheduleService.scheduleFor(WEDNESDAY)).thenReturn(
				new DaySchedule(WEDNESDAY, true, LocalTime.of(8, 0), LocalTime.of(14, 0),
						LocalTime.of(12, 0), LocalTime.of(13, 0)));

		List<LocalTime> starts = startsOf(availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(60)));

		assertThat(starts).contains(LocalTime.of(11, 0), LocalTime.of(13, 0));
		assertThat(starts).doesNotContain(LocalTime.of(11, 15), LocalTime.of(12, 0), LocalTime.of(12, 30));
	}

	@Test
	void freeSlots_closedDay_returnsNoSlotsWithoutLookingAtAppointments() {
		when(scheduleService.scheduleFor(WEDNESDAY)).thenReturn(DaySchedule.closed(WEDNESDAY));

		List<FreeSlot> slots = availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(60));

		assertThat(slots).isEmpty();
		verify(appointmentRepository, never()).findNotCancelledOverlapping(any(), any());
	}

	@Test
	void freeSlots_startsBeforeNow_areExcluded() {
		availabilityService = serviceAt(Instant.parse("2026-11-18T14:30:00Z"));
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(11, 0));

		List<LocalTime> starts = startsOf(availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(30)));

		assertThat(starts).hasSize(5);
		assertThat(starts.get(0)).isEqualTo(LocalTime.of(9, 30));
	}

	@Test
	void freeSlots_multipleDays_returnsSlotsForEachDay() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(9, 0));
		givenDay(THURSDAY, LocalTime.of(8, 0), LocalTime.of(9, 0));

		List<FreeSlot> slots = availabilityService.freeSlots(WEDNESDAY, THURSDAY, Duration.ofMinutes(60));

		assertThat(slots).hasSize(2);
		assertThat(slots.get(0).start().toLocalDate()).isEqualTo(WEDNESDAY);
		assertThat(slots.get(1).start().toLocalDate()).isEqualTo(THURSDAY);
	}

	@Test
	void freeSlots_shorterServiceHasMoreStartsThanLongerService() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

		int shortService = availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(45)).size();
		int longService = availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(180)).size();

		assertThat(shortService).isEqualTo(14);
		assertThat(longService).isEqualTo(5);
	}

	@Test
	void freeSlots_bogotaWorkingHours_queriesAppointmentsWithUtcInstants() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));

		availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ofMinutes(60));

		verify(appointmentRepository).findNotCancelledOverlapping(
				Instant.parse("2026-11-18T13:00:00Z"), Instant.parse("2026-11-18T23:00:00Z"));
	}

	@Test
	void freeSlots_zeroDuration_throwsIllegalArgument() {
		assertThatThrownBy(() -> availabilityService.freeSlots(WEDNESDAY, WEDNESDAY, Duration.ZERO))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void isAvailable_freeSlotInsideWorkingHours_returnsTrue() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));

		assertThat(availabilityService.isAvailable(at("2026-11-18T09:00-05:00"), at("2026-11-18T10:00-05:00"), null))
				.isTrue();
	}

	@Test
	void isAvailable_outsideWorkingHours_returnsFalse() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));

		assertThat(availabilityService.isAvailable(at("2026-11-18T07:30-05:00"), at("2026-11-18T08:30-05:00"), null))
				.isFalse();
		assertThat(availabilityService.isAvailable(at("2026-11-18T17:30-05:00"), at("2026-11-18T18:30-05:00"), null))
				.isFalse();
	}

	@Test
	void isAvailable_overlappingLunch_returnsFalse() {
		when(scheduleService.scheduleFor(WEDNESDAY)).thenReturn(
				new DaySchedule(WEDNESDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0),
						LocalTime.of(12, 0), LocalTime.of(13, 0)));

		assertThat(availabilityService.isAvailable(at("2026-11-18T11:30-05:00"), at("2026-11-18T12:30-05:00"), null))
				.isFalse();
	}

	@Test
	void isAvailable_overlappingAppointment_returnsFalse() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
		givenAppointments(busy("2026-11-18T09:00-05:00", "2026-11-18T10:00-05:00"));

		assertThat(availabilityService.isAvailable(at("2026-11-18T09:30-05:00"), at("2026-11-18T10:30-05:00"), null))
				.isFalse();
	}

	@Test
	void isAvailable_startingWhenAnotherEnds_returnsTrue() {
		givenDay(WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
		givenAppointments(busy("2026-11-18T09:00-05:00", "2026-11-18T10:00-05:00"));

		assertThat(availabilityService.isAvailable(at("2026-11-18T10:00-05:00"), at("2026-11-18T11:00-05:00"), null))
				.isTrue();
	}

	@Test
	void isAvailable_closedDay_returnsFalse() {
		when(scheduleService.scheduleFor(WEDNESDAY)).thenReturn(DaySchedule.closed(WEDNESDAY));

		assertThat(availabilityService.isAvailable(at("2026-11-18T09:00-05:00"), at("2026-11-18T10:00-05:00"), null))
				.isFalse();
	}

	private static Instant at(String offsetDateTime) {
		return OffsetDateTime.parse(offsetDateTime).toInstant();
	}

	private AvailabilityService serviceAt(Instant now) {
		Clock clock = Clock.fixed(now, ZoneOffset.UTC);
		return new AvailabilityService(scheduleService, appointmentRepository, clock);
	}

	private void givenDay(LocalDate day, LocalTime open, LocalTime close) {
		when(scheduleService.scheduleFor(day)).thenReturn(new DaySchedule(day, true, open, close, null, null));
	}

	private void givenAppointments(Appointment... appointments) {
		when(appointmentRepository.findNotCancelledOverlapping(any(), any())).thenReturn(List.of(appointments));
	}

	private static Appointment busy(String start, String end) {
		Client client = TestDataFactory.client("Ana", "Gómez", "3001234567");
		ServiceType mechas = TestDataFactory.serviceType("Mechas", 180);
		return TestDataFactory.appointment(client, mechas, OffsetDateTime.parse(start), OffsetDateTime.parse(end));
	}

	private static List<LocalTime> startsOf(List<FreeSlot> slots) {
		return slots.stream().map(slot -> slot.start().toLocalTime()).toList();
	}
}
