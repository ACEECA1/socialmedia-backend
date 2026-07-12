package org.socialmedia.app.controller;

import org.socialmedia.app.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ping")
public class PingController {

    public record PingRequest(@NotBlank(message = "Message cannot be blank") String message) {}

    @PostMapping
    public ApiResponse<String> ping(@Valid @RequestBody PingRequest request) {
        return ApiResponse.success(request.message() + " PONG", "Ping successful");
    }
}