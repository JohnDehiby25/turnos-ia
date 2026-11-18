package com.turnosia.repository;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turnosia.domain.WorkingHours;

public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {

	Optional<WorkingHours> findByWeekday(DayOfWeek weekday);
}
