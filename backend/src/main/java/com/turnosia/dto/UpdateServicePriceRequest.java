package com.turnosia.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

/** price NULL = "precio por confirmar". */
public record UpdateServicePriceRequest(
		@DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price) {
}
