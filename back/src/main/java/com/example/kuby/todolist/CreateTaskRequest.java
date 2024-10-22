package com.example.kuby.todolist;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String deadLine;
    private boolean isFinished;
}
