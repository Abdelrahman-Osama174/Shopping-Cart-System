package com.intern.shoppingcartsystem.infrastructure.otp;

public enum OtpVerificationResult {
    VERIFIED,
    INVALID,
    EXPIRED,
    TOO_MANY_ATTEMPTS
}