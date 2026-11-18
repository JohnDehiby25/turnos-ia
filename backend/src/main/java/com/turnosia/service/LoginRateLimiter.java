package com.turnosia.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Limita los intentos fallidos de login para frenar la fuerza bruta. Cuenta por
 * usuario y por IP; al superar el máximo se bloquea por un tiempo.
 */
@Component
public class LoginRateLimiter {

	private final Clock clock;
	private final int maxAttempts;
	private final Duration window;
	private final Duration lockout;

	private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

	public LoginRateLimiter(Clock clock, @Value("${app.security.login.max-attempts:5}") int maxAttempts,
			@Value("${app.security.login.window-minutes:15}") long windowMinutes,
			@Value("${app.security.login.lockout-minutes:15}") long lockoutMinutes) {
		this.clock = clock;
		this.maxAttempts = maxAttempts;
		this.window = Duration.ofMinutes(windowMinutes);
		this.lockout = Duration.ofMinutes(lockoutMinutes);
	}

	/** Lanza {@link BusinessRuleException} si el usuario o la IP están bloqueados. */
	public void checkAllowed(String username, String clientIp) {
		for (String key : keys(username, clientIp)) {
			Attempts recorded = attempts.get(key);
			if (recorded != null && recorded.isLocked(clock.instant())) {
				throw new BusinessRuleException("TOO_MANY_ATTEMPTS",
						"Demasiados intentos fallidos. Intenta de nuevo en unos minutos");
			}
		}
	}

	public void recordFailure(String username, String clientIp) {
		Instant now = clock.instant();
		for (String key : keys(username, clientIp)) {
			attempts.compute(key, (ignored, recorded) -> recorded == null
					? new Attempts(1, now, null)
					: recorded.registerFailure(now, window, maxAttempts, lockout));
		}
	}

	public void reset(String username, String clientIp) {
		for (String key : keys(username, clientIp)) {
			attempts.remove(key);
		}
	}

	private static String[] keys(String username, String clientIp) {
		return new String[] { "user:" + username, "ip:" + clientIp };
	}

	private record Attempts(int count, Instant firstFailure, Instant lockedUntil) {

		boolean isLocked(Instant now) {
			return lockedUntil != null && now.isBefore(lockedUntil);
		}

		Attempts registerFailure(Instant now, Duration window, int maxAttempts, Duration lockout) {
			if (now.isAfter(firstFailure.plus(window))) {
				return new Attempts(1, now, null);
			}
			int newCount = count + 1;
			Instant locked = newCount >= maxAttempts ? now.plus(lockout) : null;
			return new Attempts(newCount, firstFailure, locked);
		}
	}
}
