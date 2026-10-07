package com.internal.tasktracker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Validate paging input: page is 1-based, pageSize is capped so one request
        // cannot pull the whole table.
        if (page < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 1");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        // Normalize query input and escape LIKE wildcards so '%' and '_' match literally
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + escapeLike(query.toLowerCase()) + "%";

        // Parse status filter; an unknown value is a client error (400), not a 500
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown status: " + status);
            }
        }

        Page<Task> result = taskRepository.searchTasks(
                searchTerm, normalizedStatus, PageRequest.of(page - 1, pageSize));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
