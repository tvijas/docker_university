package com.example.kuby.controllers;

import com.example.kuby.KubyApplication;
import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.request.LoginRequest;
import com.example.kuby.todolist.CreateTaskRequest;
import com.example.kuby.todolist.Task;
import com.example.kuby.todolist.TaskRepo;
import com.example.kuby.utils.DbUtils;
import com.example.kuby.utils.JsonPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.kuby.security.util.parsers.AuthHeaderParser.parseUserIdFromAuthHeader;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = {KubyApplication.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private TaskRepo taskRepo;
    private static String authHeader;
    private static UUID taskId;
    @Autowired
    private DbUtils dbUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();
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
    public void init() throws Exception {
        UserEntity user = dbUtils.createUser();

        LoginRequest loginRequest = new LoginRequest(user.getLogin(), "18-Bad-Boy-18");

        mvc.perform(post("/api/user/login")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(var1 -> {
                    System.out.println(var1.getResponse().getHeader("Authorization"));
                    authHeader = var1.getResponse().getHeader("Authorization");
                });
    }

    @Test
    @Order(2)
    public void create_success_test() throws Exception {
        String deadLine = LocalDateTime.now().toString().replace("T", " ").split("\\.")[0];
        System.out.println(deadLine);

        CreateTaskRequest request = new CreateTaskRequest("some name", deadLine, true);

        mvc.perform(post("/api/task")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(result -> JsonPrettyPrinter.print(result.getResponse().getContentAsString()));
        taskId = taskRepo.findAllByCreatorId(parseUserIdFromAuthHeader(authHeader)).get(0).getId();
    }
    @Test
    @Order(3)
    public void update_success_test() throws Exception{
        String deadLine = LocalDateTime.now().plusYears(10).toString().replace("T", " ").split("\\.")[0];
        System.out.println(deadLine);

        CreateTaskRequest request = new CreateTaskRequest();
        request.setDeadLine(deadLine);

        mvc.perform(put("/api/task/" + taskId.toString())
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(result -> JsonPrettyPrinter.print(result.getResponse().getContentAsString()));
    }

    @Test
    @Order(4)
    public void delete_success_test() throws Exception{
        mvc.perform(delete("/api/task/" +taskId.toString())
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(5)
    public void getAll_success_test() throws  Exception{
        LocalDateTime expiredDeadline = LocalDateTime.now().plusSeconds(5);

        for(int i = 0; i<7; i++)
            taskRepo.save(Task.builder()
                            .name("soma name")
                            .deadLine(expiredDeadline)
                            .isFinished(false)
                            .creator(UserEntity.builder().id(parseUserIdFromAuthHeader(authHeader)).build())
                    .build());

        LocalDateTime nonExpiredDeadline = LocalDateTime.now().plusYears(1);

        for (int i = 0; i< 3; i++)
            taskRepo.save(Task.builder()
                    .name("soma name")
                    .deadLine(nonExpiredDeadline)
                    .isFinished(false)
                    .creator(UserEntity.builder().id(parseUserIdFromAuthHeader(authHeader)).build())
                    .build());

        Thread.sleep(5000);

        mvc.perform(get("/api/task")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(result -> JsonPrettyPrinter.print(result.getResponse().getContentAsString()));
    }
}
