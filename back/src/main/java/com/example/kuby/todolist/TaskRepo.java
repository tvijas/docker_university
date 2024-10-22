package com.example.kuby.todolist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {
    List<Task> findAllByCreatorId(UUID userId);
    Integer deleteByIdAndCreatorId(UUID taskId, UUID creatorId);
    Optional<Task> findByIdAndCreatorId(UUID taskId, UUID creatorId);
    @Modifying
    @Query("UPDATE Task t SET t.isFinished = :isFinished WHERE t.id = :id AND t.creator.id = :creatorId")
    int updateIsFinishedByIdAndCreatorId(@Param("id") UUID id, @Param("creatorId") UUID creatorId, @Param("isFinished") Boolean isFinished);
    @Modifying
    @Query(value = "UPDATE task " +
            "SET is_expired = CASE " +
            "    WHEN dead_line < :currentDateTime THEN true " +
            "    ELSE false " +
            "END " +
            "WHERE creator_id = :creatorId " +
            "RETURNING *", nativeQuery = true)
    List<Task> updateExpiredAndGetAllByCreatorId(@Param("creatorId") UUID creatorId,
                                                 @Param("currentDateTime") LocalDateTime currentDateTime);
}
