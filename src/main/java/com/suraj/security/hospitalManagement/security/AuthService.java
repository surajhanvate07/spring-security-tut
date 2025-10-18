package com.suraj.security.hospitalManagement.security;

import com.suraj.security.hospitalManagement.dto.LoginRequestDto;
import com.suraj.security.hospitalManagement.dto.LoginResponseDto;
import com.suraj.security.hospitalManagement.dto.SignUpResponseDto;
import com.suraj.security.hospitalManagement.entity.User;
import com.suraj.security.hospitalManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final AuthUtil authUtil;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword()));

        User user = (User) authentication.getPrincipal();

        String token = authUtil.generateAccessToken(user);

        return new LoginResponseDto(token, user.getId());
    }

    public SignUpResponseDto signUp(LoginRequestDto signUpRequestDto) {
        User user = userRepository.findByUsername(signUpRequestDto.getUsername()).orElse(null);
        if (user != null) throw new IllegalArgumentException("Username already exists");

        User newUser = User.builder()
                .username(signUpRequestDto.getUsername())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .build();

        userRepository.save(newUser);
        return new SignUpResponseDto(newUser.getId(), newUser.getUsername());
    }
}
