package com.turnosia.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turnosia.domain.DayOverride;

public interface DayOverrideRepository extends JpaRepository<DayOverride, LocalDate> {
}
