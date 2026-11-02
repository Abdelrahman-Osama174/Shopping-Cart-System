package com.intern.shoppingcartsystem.infrastructure.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;


    public String generateAndStore(String email) {

        String otp = String.format("%06d", RANDOM.nextInt(100000, 1000000));

        String key = buildKey(email);

        String hashedOtp = passwordEncoder.encode(otp);

        redisTemplate.opsForValue().set(key, hashedOtp, OTP_EXPIRATION);

        return otp;
    }


    public boolean verify(String email, String otp) {

        String key = buildKey(email);

        String hashedOtp = redisTemplate.opsForValue().get(key);

        if (hashedOtp == null) {
            return false;
        }

        if (!passwordEncoder.matches(otp, hashedOtp)) {
            return false;
        }

        redisTemplate.delete(key);

        return true;
    }


    private String buildKey(String email) {
        return OTP_PREFIX + email.trim().toLowerCase(Locale.ROOT);
    }
}