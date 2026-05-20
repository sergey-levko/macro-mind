package com.epam.macromind.advice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/advice")
class AiAdviceController {

    private final AiAdviceService adviceService;

    AiAdviceController(AiAdviceService adviceService) {
        this.adviceService = adviceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AiAdviceResponse generateAdvice(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody GenerateAdviceRequest request) {
        return adviceService.generateAdvice(userId, request);
    }

    @GetMapping("/{id}")
    AiAdviceResponse getAdvice(@PathVariable UUID id) {
        return adviceService.getAdvice(id);
    }

    @GetMapping
    List<AiAdviceResponse> listAdvice(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) AdviceType adviceType,
            @RequestParam(required = false) LocalDate periodStart) {
        return adviceService.listAdvice(userId, adviceType, periodStart);
    }
}
