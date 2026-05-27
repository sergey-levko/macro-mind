package com.epam.macromind.template;

import com.epam.macromind.common.SecurityUtils;
import com.epam.macromind.meal.MealLogSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/meal-templates")
class MealTemplateController {

    private final MealTemplateService service;

    MealTemplateController(MealTemplateService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MealTemplateResponse save(@Valid @RequestBody SaveTemplateRequest request) {
        return service.saveTemplate(SecurityUtils.currentUserId(), request);
    }

    @GetMapping
    List<MealTemplateResponse> list() {
        return service.listTemplates(SecurityUtils.currentUserId());
    }

    @PostMapping("/{id}/apply")
    List<MealLogSummaryResponse> apply(@PathVariable UUID id,
                                       @Valid @RequestBody ApplyTemplateRequest request) {
        return service.applyTemplate(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        service.deleteTemplate(SecurityUtils.currentUserId(), id);
    }
}
