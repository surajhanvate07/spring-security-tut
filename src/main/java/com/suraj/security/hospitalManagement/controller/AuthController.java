package com.suraj.security.hospitalManagement.controller;

import com.suraj.security.hospitalManagement.dto.LoginRequestDto;
import com.suraj.security.hospitalManagement.dto.LoginResponseDto;
import com.suraj.security.hospitalManagement.dto.SignUpResponseDto;
import com.suraj.security.hospitalManagement.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (@RequestBody LoginRequestDto loginRequestDto) {

        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup (@RequestBody LoginRequestDto signUpRequestDto) {
        return new ResponseEntity<>(authService.signUp(signUpRequestDto), HttpStatus.CREATED);

    }
}
