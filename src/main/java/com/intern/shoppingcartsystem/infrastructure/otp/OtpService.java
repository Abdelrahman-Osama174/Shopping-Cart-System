package com.intern.shoppingcartsystem.infrastructure.otp;

import com.intern.shoppingcartsystem.common.exception.OtpExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String OTP_PREFIX = "email-verification:";

    private static final Duration OTP_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration ATTEMPTS_EXPIRATION = Duration.ofMinutes(10);

    private static final int MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;


    public String generateAndStore(String email) {

        String otp = String.format("%06d", RANDOM.nextInt(100000, 1000000));

        String key = buildOtpKey(email);

        String hashedOtp = passwordEncoder.encode(otp);

        redisTemplate.opsForValue().set(key, hashedOtp, OTP_EXPIRATION);

        return otp;
    }


    public OtpVerificationResult verify(String email, String otp) {
        String otpKey = buildOtpKey(email);
        String attemptsKey = buildAttemptsKey(email);
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts == null) {
            throw new IllegalStateException("Failed to increment attempts for email: " + email);
        }

        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, ATTEMPTS_EXPIRATION);
        }

        if (attempts > MAX_ATTEMPTS) {
            redisTemplate.delete(otpKey);
            return OtpVerificationResult.TOO_MANY_ATTEMPTS;
        }

        String hashedOtp = redisTemplate.opsForValue().get(otpKey);

        if (hashedOtp == null) {
            return OtpVerificationResult.EXPIRED;
        }

        if (passwordEncoder.matches(otp, hashedOtp)) {
            deleteOtp(email);
            return OtpVerificationResult.VERIFIED;
        }

        return OtpVerificationResult.INVALID;
    }


    public boolean reserveResendCooldown(String email) {

        String cooldownKey = buildCooldownKey(email);
        Boolean reserved = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", RESEND_COOLDOWN);

        return Boolean.TRUE.equals(reserved);
    }


    public void deleteOtp(String email) {
        String emailKey = buildOtpKey(email);
        redisTemplate.delete(emailKey);

        String attemptsKey = buildAttemptsKey(email);
        redisTemplate.delete(attemptsKey);
    }


    public void releaseResendCooldown(String email) {
        String cooldownKey = buildCooldownKey(email);
        redisTemplate.delete(cooldownKey);
    }


    /// Helper methods to build Redis keys
    private String buildOtpKey(String email) {
        return OTP_PREFIX + email.trim().toLowerCase(Locale.ROOT);
    }

    private String buildAttemptsKey(String email) {
        return buildOtpKey(email) + ":attempts";
    }

    private String buildCooldownKey(String email) {
        return buildOtpKey(email) + ":cooldown";
    }

}