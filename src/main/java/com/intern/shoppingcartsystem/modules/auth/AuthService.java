package com.intern.shoppingcartsystem.modules.auth;

import com.intern.shoppingcartsystem.common.exception.EmailAlreadyExistsException;
import com.intern.shoppingcartsystem.modules.user.*;
import com.intern.shoppingcartsystem.security.JwtService;
import com.intern.shoppingcartsystem.security.UserPrincipal;
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


    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        User user = authMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(false);

        userRepository.save(user);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateJwtToken(userPrincipal);

        return authMapper.toResponse(token, user);
    }
}
