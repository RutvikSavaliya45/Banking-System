package com.example.banking.service;

import com.example.banking.dto.AuthResponse;
import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.entity.Role;
import com.example.banking.entity.User;
import com.example.banking.exception.ApiException;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.JwtService;
import com.example.banking.security.UserPrincipal;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.ROLE_USER));
        User saved = userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(new UserPrincipal(saved)));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return new AuthResponse(jwtService.generateToken(new UserPrincipal(user)));
    }
}
