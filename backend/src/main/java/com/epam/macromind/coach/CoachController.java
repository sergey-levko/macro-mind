package com.epam.macromind.coach;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
class CoachController {

    private final CoachService coachService;

    CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @PostMapping
    ChatResponse chat(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody ChatRequest request) {
        return coachService.chat(userId, request.message());
    }
}
