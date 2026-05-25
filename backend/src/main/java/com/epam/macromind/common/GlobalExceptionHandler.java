package com.epam.macromind.common;

import com.epam.macromind.advice.AdviceNotFoundException;
import com.epam.macromind.advice.NoGoalForAdviceException;
import com.epam.macromind.auth.InvalidCredentialsException;
import com.epam.macromind.food.FoodAccessDeniedException;
import com.epam.macromind.food.FoodInUseException;
import com.epam.macromind.goal.GoalGenerationException;
import com.epam.macromind.goal.GoalNotFoundException;
import com.epam.macromind.food.FoodNotFoundException;
import com.epam.macromind.food.UsdaFoodNotFoundException;
import com.epam.macromind.food.UsdaServiceUnavailableException;
import com.epam.macromind.meal.MealItemNotFoundException;
import com.epam.macromind.meal.MealLogAccessDeniedException;
import com.epam.macromind.meal.MealLogNotFoundException;
import com.epam.macromind.user.EmailAlreadyExistsException;
import com.epam.macromind.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleNotFound(UserNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> handleConflict(EmailAlreadyExistsException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid"));
        return Map.of("message", "Validation failed", "errors", fieldErrors);
    }

    @ExceptionHandler(FoodNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleFoodNotFound(FoodNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(FoodAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Map<String, String> handleFoodAccessDenied(FoodAccessDeniedException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(FoodInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> handleFoodInUse(FoodInUseException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(UsdaFoodNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleUsdaFoodNotFound(UsdaFoodNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(UsdaServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    Map<String, String> handleUsdaUnavailable(UsdaServiceUnavailableException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(MealLogNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleMealLogNotFound(MealLogNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(MealLogAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Map<String, String> handleMealLogAccessDenied(MealLogAccessDeniedException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(MealItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleMealItemNotFound(MealItemNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(GoalNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleGoalNotFound(GoalNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(GoalGenerationException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    Map<String, String> handleGoalGenerationFailed(GoalGenerationException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(AdviceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleAdviceNotFound(AdviceNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(NoGoalForAdviceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> handleNoGoalForAdvice(NoGoalForAdviceException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Map<String, String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> handleBadRequest(Exception ex) {
        return Map.of("message", "Invalid request: " + ex.getMessage());
    }
}
