package com.epam.macromind.advice;

import com.epam.macromind.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/advice")
class AiAdviceController {

    private final AiAdviceService adviceService;

    AiAdviceController(AiAdviceService adviceService) {
        this.adviceService = adviceService;
    }

    @PostMapping
    ResponseEntity<AiAdviceResponse> generateAdvice(@Valid @RequestBody GenerateAdviceRequest request) {
        GenerateAdviceResult result = adviceService.generateAdvice(SecurityUtils.currentUserId(), request);
        return result.created()
                ? ResponseEntity.status(HttpStatus.ACCEPTED).body(result.response())
                : ResponseEntity.ok(result.response());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteAdvice(@PathVariable java.util.UUID id) {
        adviceService.deleteAdvice(SecurityUtils.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    AiAdviceResponse getAdvice(@PathVariable java.util.UUID id) {
        return adviceService.getAdvice(id);
    }

    @GetMapping
    List<AiAdviceResponse> listAdvice(
            @RequestParam(required = false) AdviceType adviceType,
            @RequestParam(required = false) LocalDate periodStart) {
        return adviceService.listAdvice(SecurityUtils.currentUserId(), adviceType, periodStart);
    }
}
