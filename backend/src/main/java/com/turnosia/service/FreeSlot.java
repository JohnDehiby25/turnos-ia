package com.turnosia.service;

import java.time.ZonedDateTime;

public record FreeSlot(ZonedDateTime start, ZonedDateTime end) {
}
