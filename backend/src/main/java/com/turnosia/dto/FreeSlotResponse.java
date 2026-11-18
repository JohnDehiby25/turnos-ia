package com.turnosia.dto;

import java.time.Instant;

import com.turnosia.service.FreeSlot;

public record FreeSlotResponse(Instant start, Instant end) {

	public static FreeSlotResponse from(FreeSlot slot) {
		return new FreeSlotResponse(slot.start().toInstant(), slot.end().toInstant());
	}
}
