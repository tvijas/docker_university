package com.example.kuby.controllers;

import com.example.kuby.KubyApplication;
import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.enums.Provider;
import com.example.kuby.security.models.request.ChangePasswordRequest;
import com.example.kuby.security.models.request.LoginRequest;
import com.example.kuby.security.models.request.SignUpRequest;
import com.example.kuby.foruser.UserRepo;
import com.example.kuby.security.util.generate.GenerateCode;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest(classes = {KubyApplication.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAuthControllerTest {

    @Value("${spring.mail.username}")
    private String email;
    @Autowired
    private UserRepo userRepo;
    @MockBean
    private GenerateCode generateCode;
    @Autowired
    private PasswordEncoder encoder;
    private static String authHeader;
    @Autowired
    private MockMvc mvc;
    private final ObjectMapper objMapper = new ObjectMapper();

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));
    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        redis.start();
    }
    @AfterAll
    static void afterAll() {
        postgres.stop();
        redis.stop();
    }

    @Test
    @Order(1)
    void initialData() throws Exception {
        Mockito.when(this.generateCode.generateCode()).thenReturn("STRING_PIZDATIY_CODE");
        SignUpRequest signUpRequest = new SignUpRequest(email, "lox322228","fsfsDSF@545AADFDGEWE3AR");

        mvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/user/verify/local")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("code","STRING_PIZDATIY_CODE")
                        .param("email",email))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("lox322228", "fsfsDSF@545AADFDGEWE3AR");

        mvc.perform(post("/api/user/login")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(var1 -> {
                    System.out.println( var1.getResponse().getHeader("Authorization"));
                    authHeader = var1.getResponse().getHeader("Authorization");
                });
    }

    @Test
    @Order(2)
    void test_secured_endpoint_success() throws Exception {
        mvc.perform(post("/testing")
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(3)
    void test_wrong_domain_failure() throws Exception{
        SignUpRequest signUpRequest = new SignUpRequest("progamer2015@fimoz.club","lox228322","fsfsDSF@545AADFDGEWE3AR");
        mvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['email']").value("Email doesn't exists"))
                .andReturn().getResponse().getContentAsString();
    }
    @Test
    @Order(4)
    void test_password_refresh_endpoints_success() throws Exception{
        Mockito.when(this.generateCode.generateCode()).thenReturn("STRING_PIZDATIY_CODE");
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(email,"123FimozikZ1GH4AIL###41@34");

        mvc.perform(post("/api/user/change-password")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isAccepted());

        mvc.perform(get("/api/user/summit-password-change")
                .param("code","STRING_PIZDATIY_CODE")
                .param("email",email))
                .andExpect(status().isOk())
                .andExpect(var1 -> {
                    UserEntity user = userRepo.findByEmailAndProvider(email,Provider.LOCAL).get();
                    encoder.encode(user.getPassword()).equals("123FimozikZ1GH4AIL###41@34");
                });
    }
}
