package com.example.kuby.utils;

import com.example.kuby.todolist.Task;
import com.example.kuby.todolist.TaskDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Mapper {
    private final ModelMapper mapper;
    public TaskDTO convertTaskToDTO(Task task){
        return mapper.map(task, TaskDTO.class);
    }
}
