package com.example.kuby.todolist;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskDTO {
    private UUID id;
    private UUID creatorId;
    private String name;
    private String deadLine;
    private Boolean isFinished;
    private Boolean isExpired;
}
