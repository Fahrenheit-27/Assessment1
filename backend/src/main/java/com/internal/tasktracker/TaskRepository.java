package com.internal.tasktracker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // The title/description match is wrapped in parentheses so that the archived and
    // status filters apply to BOTH branches of the OR. Without them, AND binds tighter
    // than OR and archived tasks leak through and the status filter is ignored for
    // title matches. Paging happens in the database (LIMIT/OFFSET) instead of in memory.
    @Query(value = "SELECT * FROM tasks "
                 + "WHERE archived = FALSE "
                 + "AND (LOWER(title) LIKE :term ESCAPE '\\' OR LOWER(description) LIKE :term ESCAPE '\\') "
                 + "AND (CAST(:status AS VARCHAR(20)) IS NULL OR status = :status) "
                 + "ORDER BY created_at DESC, id DESC",
           countQuery = "SELECT COUNT(*) FROM tasks "
                      + "WHERE archived = FALSE "
                      + "AND (LOWER(title) LIKE :term ESCAPE '\\' OR LOWER(description) LIKE :term ESCAPE '\\') "
                      + "AND (CAST(:status AS VARCHAR(20)) IS NULL OR status = :status)",
           nativeQuery = true)
    Page<Task> searchTasks(@Param("term") String term,
                           @Param("status") String status,
                           Pageable pageable);
}
