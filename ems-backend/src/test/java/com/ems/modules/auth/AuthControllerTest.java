package com.ems.modules.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ems.common.enums.Role;
import com.ems.modules.auth.dto.LoginRequest;
import com.ems.modules.auth.dto.RegisterRequest;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FactoryRepository factoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Factory testFactory;
    private User testAdmin;
    private User testUser;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        factoryRepository.deleteAll();

        testFactory = Factory.builder()
                .name("Test Factory")
                .address("123 Test St")
                .timezone("Asia/Kolkata")
                .contractDemandKw(BigDecimal.valueOf(500))
                .tariffInrPerKwh(BigDecimal.valueOf(7.5))
                .build();
        testFactory = factoryRepository.save(testFactory);

        testAdmin = User.builder()
                .email("admin@test.local")
                .passwordHash(passwordEncoder.encode("AdminPass123"))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .factory(testFactory)
                .isActive(true)
                .build();
        userRepository.save(testAdmin);

        testUser = User.builder()
                .email("user@test.local")
                .passwordHash(passwordEncoder.encode("UserPass123"))
                .firstName("Regular")
                .lastName("User")
                .role(Role.VIEWER)
                .factory(testFactory)
                .isActive(true)
                .build();
        userRepository.save(testUser);
    }

    @Test
    public void login_success() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("user@test.local")
                .password("UserPass123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("user@test.local"));
    }

    @Test
    public void login_wrongPassword_401() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("user@test.local")
                .password("WrongPass")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "user@test.local", roles = {"VIEWER"})
    public void register_nonAdmin_403() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .email("newuser@test.local")
                .password("NewPass123")
                .firstName("New")
                .lastName("User")
                .role(Role.VIEWER)
                .factoryId(testFactory.getId())
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.local", roles = {"ADMIN"})
    public void register_admin_success() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .email("newuser@test.local")
                .password("NewPass123")
                .firstName("New")
                .lastName("User")
                .role(Role.VIEWER)
                .factoryId(testFactory.getId())
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newuser@test.local"));
    }
}
