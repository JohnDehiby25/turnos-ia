package com.turnosia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turnosia.domain.ServiceType;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {

	List<ServiceType> findAllByActiveTrueOrderByNameAsc();
}
