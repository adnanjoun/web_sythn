package com.syntheaweb.backend.controller;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.User;
import com.syntheaweb.backend.database.repository.UserRepository;
import com.syntheaweb.backend.service.JwtUtil;
import com.syntheaweb.backend.service.RunService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing synthetic data runs
 */
@RestController 
@RequestMapping("/api/runs")
public class RunController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final RunService runService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public RunController(RunService runService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.runService = runService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * get all runs associated with the authenticated user
     *
     * @param token The JWT token provided in the Authorization header.
     * @return ResponseEntity containing the list of runs for the user, or an error status
     */
    @GetMapping
    public ResponseEntity<List<Run>> getUserRuns(@RequestHeader("Authorization") String token) {
        return getAuthenticatedUser(token)
                .map(user -> ResponseEntity.ok(runService.getRunsByUser(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * get all runs in the system (as admin).
     *
     * @param token The JWT token provided in the Authorization header.
     * @return ResponseEntity containing the list of all runs, or an error status
     */
    @GetMapping("/admin")
    public ResponseEntity<List<Run>> getAllRuns(@RequestHeader("Authorization") String token) {
        return getAuthenticatedUser(token)
                .filter(user -> ROLE_ADMIN.equals(user.getRole()))
                .map(user -> ResponseEntity.ok(runService.getAllRuns()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    /**
     * Deletes a run by its ID
     *
     * @param runId The ID of the run to delete
     * @param token The JWT token provided in the Authorization header.
     * @return ResponseEntity returning the result of the delete operation
     */
    @DeleteMapping("/delete/{runId}")
    public ResponseEntity<Void> deleteRun(@PathVariable String runId, @RequestHeader("Authorization") String token) {
        Optional<User> authenticatedUser = getAuthenticatedUser(token);

        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = authenticatedUser.get();

        if (!ROLE_ADMIN.equals(user.getRole()) && !runService.isRunOwner(runId, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = runService.deleteRun(runId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * Saves a new run for the authenticated user
     *
     * @param run   the run to save.
     * @param token The JWT token provided in the Authorization header.
     * @return ResponseEntity returning the result of the save operation
     */
    @PostMapping("/save")
    public ResponseEntity<String> saveRun(@RequestBody Run run, @RequestHeader("Authorization") String token) {
        return getAuthenticatedUser(token)
                .map(user -> {
                    runService.saveRun(run, user);
                    return ResponseEntity.status(HttpStatus.CREATED).body("Synthea run saved successfully!");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }


    /**
     * Extracts the authenticated user from the provided JWT token
     *
     * @param token The JWT token provided in the Authorization header.
     * @return An Optional containing the authenticated user, or empty if user not found
     */
    private Optional<User> getAuthenticatedUser(String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", "").trim());
        return userRepository.findByUsername(username);
    }
}
