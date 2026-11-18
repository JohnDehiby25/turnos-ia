package com.turnosia.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.turnosia.TestcontainersConfiguration;
import com.turnosia.domain.DayOverride;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DayOverrideRepositoryTest {

	@Autowired
	private DayOverrideRepository dayOverrideRepository;

	@Test
	void save_openSundayWithoutLunch_roundTrips() {
		LocalDate date = LocalDate.of(2026, 11, 22);

		dayOverrideRepository.save(new DayOverride(date, true,
				LocalTime.of(9, 0), LocalTime.of(15, 0), null, null, "Domingo trabajado"));

		Optional<DayOverride> found = dayOverrideRepository.findById(date);

		assertThat(found).isPresent();
		assertThat(found.get().isOpen()).isTrue();
		assertThat(found.get().getOpenTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(found.get().getLunchStart()).isNull();
		assertThat(found.get().getNote()).isEqualTo("Domingo trabajado");
	}

	@Test
	void save_closedDay_roundTrips() {
		LocalDate date = LocalDate.of(2026, 12, 25);

		dayOverrideRepository.save(new DayOverride(date, false, null, null, null, null, "Cerrado"));

		Optional<DayOverride> found = dayOverrideRepository.findById(date);

		assertThat(found).isPresent();
		assertThat(found.get().isOpen()).isFalse();
		assertThat(found.get().getOpenTime()).isNull();
	}
}
