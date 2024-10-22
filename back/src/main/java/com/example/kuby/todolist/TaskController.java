package com.example.kuby.todolist;

import com.example.kuby.foruser.UserEntity;
import com.example.kuby.utils.LocalDateTimeParser;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDTO> create(@RequestBody @Valid CreateTaskRequest request,
                                          @Parameter(hidden = true) @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(
                request.getName(),
                user.getId(),
                LocalDateTimeParser.parse(request.getDeadLine()),
                request.isFinished()
        ));
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAll(@Parameter(hidden = true) @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(taskService.getAll(user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable UUID id,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(required = false) String deadLine,
                                          @Parameter(hidden = true) @AuthenticationPrincipal UserEntity user) {
        LocalDateTime formattedDeadline = null;
        if (deadLine != null)
            formattedDeadline = LocalDateTimeParser.parse(deadLine);

        return ResponseEntity.ok(taskService.update(id, name, formattedDeadline, user.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> finish(@PathVariable UUID id,@Parameter(hidden = true) @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(taskService.finish(id, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @Parameter(hidden = true) @AuthenticationPrincipal UserEntity user) {
        taskService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
