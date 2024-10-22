package com.example.kuby.todolist;

import com.example.kuby.exceptions.BasicException;
import com.example.kuby.foruser.UserEntity;
import com.example.kuby.utils.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepo taskRepo;
    private final Mapper mapper;

    public TaskDTO create(String name, UUID userId, LocalDateTime deadLine, Boolean isFinished) {
        return mapper.convertTaskToDTO(taskRepo.save(Task.builder()
                .name(name)
                .creator(UserEntity.builder()
                        .id(userId)
                        .build())
                .deadLine(deadLine)
                .isFinished(isFinished)
                .build()));
    }

    @Transactional
    public List<TaskDTO> getAll(UUID userId) {
        return taskRepo.updateExpiredAndGetAllByCreatorId(userId, LocalDateTime.now()).stream()
                .map(mapper::convertTaskToDTO)
                .toList();
    }

    @Transactional
    public TaskDTO update(UUID id, String name, LocalDateTime deadLine, UUID userId) {
        Task task = taskRepo.findByIdAndCreatorId(id, userId).orElseThrow(() ->
                new BasicException(Map.of("id", "Task with such id not found"), HttpStatus.NOT_FOUND));

        if (name != null)
            task.setName(name);
        if (deadLine != null)
            task.setDeadLine(deadLine);

        return mapper.convertTaskToDTO(taskRepo.save(task));
    }

    @Transactional
    public TaskDTO finish(UUID id, UUID userId) {
        if (taskRepo.updateIsFinishedByIdAndCreatorId(id, userId, true) != 1)
            throw new BasicException(Map.of("taskId", "Task with such id not found"), HttpStatus.NOT_FOUND);

        return mapper.convertTaskToDTO(taskRepo.findByIdAndCreatorId(id, userId).orElseThrow(() ->
                new BasicException(Map.of("taskId", "Task with such id not found"), HttpStatus.NOT_FOUND)));
    }

    @Transactional
    public void delete(UUID taskId, UUID userId) {
        if (taskRepo.deleteByIdAndCreatorId(taskId, userId) != 1)
            throw new BasicException(Map.of("taskId", "Task with such id not found"), HttpStatus.NOT_FOUND);
    }
}
