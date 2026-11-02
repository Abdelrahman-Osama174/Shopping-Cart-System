package com.intern.shoppingcartsystem.modules.auth;

import com.intern.shoppingcartsystem.common.exception.EmailAlreadyExistsException;
import com.intern.shoppingcartsystem.common.exception.OtpExpiredException;
import com.intern.shoppingcartsystem.common.exception.ResourceNotFoundException;
import com.intern.shoppingcartsystem.infrastructure.email.EmailService;
import com.intern.shoppingcartsystem.infrastructure.otp.OtpService;
import com.intern.shoppingcartsystem.modules.user.*;
import com.intern.shoppingcartsystem.security.JwtService;
import com.intern.shoppingcartsystem.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
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

        return authMapper.toResponse(token, user);
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


    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyExistsException("Email is already verified");
        }

        boolean isValid = otpService.verify(request.email(), request.otp());

        if (!isValid) {
            throw new OtpExpiredException("Invalid or expired OTP");
        }

        user.setEmailVerified(true);

        userRepository.save(user);
    }


    private void sendVerificationOtp(String email) {

        String otp = otpService.generateAndStore(email);

        emailService.sendEmail(
                email,
                "Verify Your Email - Shopping Cart System",
                """
                        Hello,
                        
                        Your email verification code is: %s
                        
                        This code will expire in 5 minutes.
                        
                        If you did not create an account, please ignore this email.
                        """.formatted(otp)
        );
    }

}
