package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.security.*;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.repository.RoleRepository;
import com.example.sklepElektroniczny.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateUser_Success() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtTokenUtil.generateTokenFromUsername("testuser"))
                .thenReturn("dummy-jwt-token");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().get("Set-Cookie")).isNotEmpty();
        assertThat(response.getBody()).isInstanceOf(UserInfoResponse.class);

        UserInfoResponse body = (UserInfoResponse) response.getBody();
        assertThat(body.getUsername()).isEqualTo("testuser");
        assertThat(body.getJwtToken()).isEqualTo("dummy-jwt-token");  // <- poprawione tutaj
    }

    @Test
    void testAuthenticateUser_BadCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrong");
        loginRequest.setPassword("wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Bad credentials");
        assertThat(body.get("status")).isEqualTo(false);
    }

    @Test
    void testRegisterUser_UsernameExists() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existingUser");
        signupRequest.setEmail("newemail@example.com");
        signupRequest.setPassword("pass123");

        when(userRepository.existsByUserName("existingUser")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertThat(messageResponse.getMessage()).contains("Username is already taken");
    }

    @Test
    void testRegisterUser_EmailExists() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("existingemail@example.com");
        signupRequest.setPassword("pass123");

        when(userRepository.existsByUserName("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existingemail@example.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertThat(messageResponse.getMessage()).contains("Email is already in use");
    }

}


