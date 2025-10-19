package com.suraj.security.hospitalManagement.security;

import com.suraj.security.hospitalManagement.dto.LoginRequestDto;
import com.suraj.security.hospitalManagement.dto.LoginResponseDto;
import com.suraj.security.hospitalManagement.dto.SignUpResponseDto;
import com.suraj.security.hospitalManagement.entity.User;
import com.suraj.security.hospitalManagement.entity.type.AuthProviderType;
import com.suraj.security.hospitalManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    public User userSignUp(LoginRequestDto signUpRequestDto, AuthProviderType providerType, String providerId) {
        User user = userRepository.findByUsername(signUpRequestDto.getUsername()).orElse(null);
        if (user != null) throw new IllegalArgumentException("Username already exists");

        User newUser = User.builder()
                .username(signUpRequestDto.getUsername())
                .providerType(providerType)
                .providerId(providerId)
                .build();

        if (providerType == AuthProviderType.EMAIL) {
            newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        } else {
            newUser.setPassword("");
        }
        return userRepository.save(newUser);
    }

    public SignUpResponseDto signUp(LoginRequestDto signUpRequestDto) {
        User newUser = userSignUp(signUpRequestDto, AuthProviderType.EMAIL, null);
        return new SignUpResponseDto(newUser.getId(), newUser.getUsername());
    }

    public ResponseEntity<LoginResponseDto> handleOAuth2Login(OAuth2User oAuth2User, String registrationId) {
        // providerType google, github
        // providerId -> sub, id
        // if user exists, login, generate token
        // if not, create user and save, login and generate token

        AuthProviderType providerType = authUtil.getProviderTypeFromRegistrationId(registrationId);
        String providerId = authUtil.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);

        String email = oAuth2User.getAttribute("email");

        User emailUser = userRepository.findByUsername(email).orElse(null);

        if (user == null && emailUser == null) {
            String username = authUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId);
            user = userSignUp(new LoginRequestDto(username, null), providerType, providerId);
        } else if (user != null) {
            if (email != null && !email.isBlank() && !user.getUsername().equals(email)) {
                user.setUsername(email);
                userRepository.save(user);
            }
        } else {
            throw new BadCredentialsException("Email " + email + " is already registered with provider : " + emailUser.getProviderType() + ".Please login using your credentials.");
        }

        LoginResponseDto loginResponseDto = new LoginResponseDto(authUtil.generateAccessToken(user), user.getId());

        return ResponseEntity.status(HttpStatus.OK).body(loginResponseDto);
    }
}
