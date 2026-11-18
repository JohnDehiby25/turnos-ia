package com.turnosia.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.WorkingHours;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkingHoursRepositoryTest {

	@Autowired
	private WorkingHoursRepository workingHoursRepository;

	@Test
	void save_openDayWithLunch_roundTripsThroughDayOfWeekConverter() {
		workingHoursRepository.save(new WorkingHours(DayOfWeek.MONDAY, true,
				LocalTime.of(8, 0), LocalTime.of(18, 0),
				LocalTime.of(12, 0), LocalTime.of(13, 0)));

		Optional<WorkingHours> found = workingHoursRepository.findByWeekday(DayOfWeek.MONDAY);

		assertThat(found).isPresent();
		assertThat(found.get().getWeekday()).isEqualTo(DayOfWeek.MONDAY);
		assertThat(found.get().isOpen()).isTrue();
		assertThat(found.get().getLunchStart()).isEqualTo(LocalTime.of(12, 0));
		assertThat(found.get().getLunchEnd()).isEqualTo(LocalTime.of(13, 0));
	}

	@Test
	void save_closedDayWithoutTimes_isAllowed() {
		workingHoursRepository.save(new WorkingHours(DayOfWeek.SUNDAY, false,
				null, null, null, null));

		Optional<WorkingHours> found = workingHoursRepository.findByWeekday(DayOfWeek.SUNDAY);

		assertThat(found).isPresent();
		assertThat(found.get().isOpen()).isFalse();
		assertThat(found.get().getOpenTime()).isNull();
	}
}
