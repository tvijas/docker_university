package com.example.kuby.config;

import com.example.kuby.foruser.UserEntity;
import com.example.kuby.todolist.Task;
import com.example.kuby.todolist.TaskDTO;
import com.example.kuby.utils.LocalDateTimeFormatter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.modelmapper.config.Configuration.AccessLevel.PRIVATE;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper getMapper(){
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(PRIVATE);

        mapper.typeMap(Task.class, TaskDTO.class)
                .addMappings(mapping -> {
                    mapping.map(Task::getCreator, TaskDTO::setCreatorId);
                    mapping.map(Task::getDeadLine, TaskDTO::setDeadLine);
                });
        mapper.createTypeMap(UserEntity.class, UUID.class)
                .setConverter(context -> context.getSource() == null ? null : context.getSource().getId());

        mapper.createTypeMap(LocalDateTime.class, String.class)
                .setConverter(context -> {
                    LocalDateTime source = context.getSource();
                    return source == null ? null : LocalDateTimeFormatter.convertToString(source);
                });

        return mapper;
        }
}
