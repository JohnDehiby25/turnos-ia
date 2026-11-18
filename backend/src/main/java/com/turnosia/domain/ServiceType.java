package com.turnosia.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_type")
public class ServiceType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	/** Duración estimada por defecto; la cita define la duración real. */
	@Column(name = "duration_min", nullable = false)
	private Integer durationMin;

	/** NULL = "precio por confirmar"; se define por caso al crear la cita. */
	@Column(precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private boolean active = true;

	protected ServiceType() {
	}

	public ServiceType(String name, Integer durationMin, BigDecimal price) {
		this.name = name;
		this.durationMin = durationMin;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getDurationMin() {
		return durationMin;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public boolean isActive() {
		return active;
	}
}
