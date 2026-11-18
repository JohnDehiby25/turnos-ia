package com.turnosia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.turnosia.config.AppProperties;

class LoginRateLimiterTest {

	private static final String USERNAME = "admin";
	private static final String IP = "192.0.2.10";

	private MutableClock clock;
	private LoginRateLimiter rateLimiter;

	@BeforeEach
	void setUp() {
		clock = new MutableClock(Instant.parse("2026-11-18T14:00:00Z"));
		rateLimiter = new LoginRateLimiter(clock, loginProperties(3, 15, 15));
	}

	@Test
	void checkAllowed_noFailuresYet_doesNotThrow() {
		rateLimiter.checkAllowed(USERNAME, IP);
	}

	@Test
	void checkAllowed_afterMaxFailures_throwsTooManyAttempts() {
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);

		assertThatThrownBy(() -> rateLimiter.checkAllowed(USERNAME, IP))
				.isInstanceOfSatisfying(BusinessRuleException.class,
						e -> assertThat(e.getCode()).isEqualTo("TOO_MANY_ATTEMPTS"));
	}

	@Test
	void checkAllowed_belowMaxFailures_doesNotThrow() {
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);

		rateLimiter.checkAllowed(USERNAME, IP);
	}

	@Test
	void checkAllowed_afterLockoutExpires_isAllowedAgain() {
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		clock.advance(Duration.ofMinutes(16));

		rateLimiter.checkAllowed(USERNAME, IP);
	}

	@Test
	void checkAllowed_differentUserFromSameIp_isBlocked() {
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);

		assertThatThrownBy(() -> rateLimiter.checkAllowed("otra", IP))
				.isInstanceOf(BusinessRuleException.class);
	}

	@Test
	void checkAllowed_sameUserFromDifferentIp_isBlocked() {
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);

		assertThatThrownBy(() -> rateLimiter.checkAllowed(USERNAME, "198.51.100.7"))
				.isInstanceOf(BusinessRuleException.class);
	}

	@Test
	void reset_clearsFailures() {
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.reset(USERNAME, IP);

		rateLimiter.checkAllowed(USERNAME, IP);
	}

	@Test
	void recordFailure_afterWindow_clearsOldCount() {
		rateLimiter.recordFailure(USERNAME, IP);
		rateLimiter.recordFailure(USERNAME, IP);
		clock.advance(Duration.ofMinutes(16));
		rateLimiter.recordFailure(USERNAME, IP);

		rateLimiter.checkAllowed(USERNAME, IP);
	}

	private static AppProperties loginProperties(int maxAttempts, long windowMinutes, long lockoutMinutes) {
		return new AppProperties(
				new AppProperties.Security(new AppProperties.Security.Cors(List.of()),
						new AppProperties.Security.Login(maxAttempts, windowMinutes, lockoutMinutes)),
				new AppProperties.Admin("admin", ""));
	}

	private static final class MutableClock extends Clock {

		private Instant instant;

		MutableClock(Instant instant) {
			this.instant = instant;
		}

		void advance(Duration duration) {
			instant = instant.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}
	}
}
