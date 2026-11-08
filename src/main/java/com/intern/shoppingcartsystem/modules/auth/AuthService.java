package com.intern.shoppingcartsystem.modules.auth;

import com.intern.shoppingcartsystem.common.exception.*;
import com.intern.shoppingcartsystem.infrastructure.email.EmailService;
import com.intern.shoppingcartsystem.infrastructure.otp.OtpService;
import com.intern.shoppingcartsystem.infrastructure.otp.OtpVerificationResult;
import com.intern.shoppingcartsystem.modules.user.*;
import com.intern.shoppingcartsystem.security.JwtService;
import com.intern.shoppingcartsystem.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final AuthMapper authMapper;

    private final OtpService otpService;
    private final EmailService emailService;


    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateJwtToken(userDetails);

        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        User user = userPrincipal.getUser();

        AuthResponse response = authMapper.toResponse(token, user);
        return response;
    }


    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        User user = authMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(false);

        userRepository.save(user);

        sendVerificationOtp(user.getEmail());

        return "Registration successful. Please verify your email.";
    }


    public void verifyEmail(VerifyEmailRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        OtpVerificationResult result = otpService.verify(request.email(), request.otp());

        switch (result) {
            case VERIFIED -> user.setEmailVerified(true);
            case EXPIRED -> throw new OtpExpiredException("OTP has expired. Please request a new one.");
            case INVALID -> throw new OtpInvalidException("Invalid OTP. Please check the code and try again.");
            case TOO_MANY_ATTEMPTS ->
                    throw new OtpAttemptsExceededException("Maximum OTP verification attempts exceeded. Please try again in 10 minutes.");
        }

        userRepository.save(user);
    }


    public void resendVerificationOtp(String email) {

        String emailLower = email.trim().toLowerCase();

        User user = userRepository.findByEmail(emailLower).orElse(null);

        if (user == null || user.isEmailVerified()) {
            return;
        }

        sendVerificationOtp(emailLower);
    }


    /// Helper method
    private void sendVerificationOtp(String email) {

        boolean reserved = otpService.reserveResendCooldown(email);

        if (!reserved) {
            throw new OtpCooldownException("Please wait 60 seconds before requesting another OTP");
        }

        try {

            String otp = otpService.generateAndStore(email);

            emailService.sendEmail(
                    email,
                    "Verify Your Email - Shopping Cart System",
                    """ 
                            Hello,
                            Your verification code is: %s
                            This code expires in 5 minutes.
                            If you did not create an account, please ignore this email.
                            """.formatted(otp)

            );


        } catch (RuntimeException exception) {

            otpService.deleteOtp(email);
            otpService.releaseResendCooldown(email);

            throw exception;
        }
    }

}
