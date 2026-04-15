package com.splito.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
// These Health checks are already managed through actuator, this is just for learning
        Map<String, Object> response = new HashMap<>();

        // DB Check
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("db", result == 1 ? "UP" : "DOWN");
        } catch (Exception e) {
            response.put("db", "DOWN");
            response.put("dbError", e.getMessage());
        }

        // Redis Check
//        try {
//            redisTemplate.opsForValue().set("health-check", "ok");
//            String value = (String) redisTemplate.opsForValue().get("health-check");
//            response.put("redis", "ok".equals(value) ? "UP" : "DOWN");
//        } catch (Exception e) {
//            response.put("redis", "DOWN");
//            response.put("redisError", e.getMessage());
//        }

        return ResponseEntity.ok(response);
    }
}