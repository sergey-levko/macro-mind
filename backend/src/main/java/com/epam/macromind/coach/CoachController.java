package com.epam.macromind.coach;

import com.epam.macromind.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
class CoachController {

    private final CoachService coachService;

    CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @PostMapping
    ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return coachService.chat(SecurityUtils.currentUserId(), request.message());
    }
}
