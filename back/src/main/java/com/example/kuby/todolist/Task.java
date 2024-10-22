package com.example.kuby.todolist;

import com.example.kuby.foruser.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
@org.springframework.data.relational.core.mapping.Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id",referencedColumnName = "id")
    private UserEntity creator;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private LocalDateTime deadLine;
    @Column(columnDefinition = "BOOLEAN DEFAULT false",nullable = false)
    private Boolean isFinished;
    @Column(nullable = false)
    private Boolean isExpired;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        setIsExpired(!deadLine.isAfter(LocalDateTime.now()));
    }
}
