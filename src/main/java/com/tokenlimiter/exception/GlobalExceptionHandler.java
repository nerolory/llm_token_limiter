package com.tokenlimiter.exception;

import java.net.URI;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler.
 * Converts domain and system errors into RFC 7807 responses ({@link ProblemDetail})
 * using pattern matching {@code switch}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final URI INSUFFICIENT_CREDITS_TYPE =
            URI.create("https://api.tokenlimiter.com/problems/insufficient-credits");
    private static final URI USER_NOT_FOUND_TYPE =
            URI.create("https://api.tokenlimiter.com/problems/user-not-found");
    private static final URI VALIDATION_ERROR_TYPE =
            URI.create("https://api.tokenlimiter.com/problems/validation-error");
    private static final URI INVALID_MODEL_TYPE =
            URI.create("https://api.tokenlimiter.com/problems/invalid-model");
    private static final URI INTERNAL_ERROR_TYPE =
            URI.create("https://api.tokenlimiter.com/problems/internal-error");

    /**
     * Maps known exceptions to RFC 7807 problem details via pattern matching.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception ex, WebRequest request) {
        return switch (ex) {
            case InsufficientCreditsException ice -> buildResponse(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Insufficient Credits",
                    INSUFFICIENT_CREDITS_TYPE,
                    ice.getMessage(),
                    request,
                    problem -> {
                        problem.setProperty("userId", ice.getUserId());
                        problem.setProperty("requestedTokens", ice.getRequestedTokens());
                        problem.setProperty("availableTokens", ice.getAvailableTokens());
                    });

            case UserNotFoundException unfe -> buildResponse(
                    HttpStatus.NOT_FOUND,
                    "User Not Found",
                    USER_NOT_FOUND_TYPE,
                    unfe.getMessage(),
                    request,
                    problem -> problem.setProperty("userId", unfe.getUserId()));

            case InvalidModelException ime -> buildResponse(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Invalid Model",
                    INVALID_MODEL_TYPE,
                    ime.getMessage(),
                    request,
                    problem -> problem.setProperty("targetModel", ime.getTargetModel()));

            case IllegalArgumentException iae -> buildResponse(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Invalid Model",
                    INVALID_MODEL_TYPE,
                    iae.getMessage() != null
                            ? iae.getMessage()
                            : "Unsupported target model",
                    request,
                    problem -> {});

            case MethodArgumentNotValidException validEx -> buildValidationResponse(validEx, request);

            default -> {
                log.error("Unhandled exception", ex);
                yield buildResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        INTERNAL_ERROR_TYPE,
                        "An unexpected error occurred",
                        request,
                        problem -> {});
            }
        };
    }

    private ResponseEntity<ProblemDetail> buildValidationResponse(
            MethodArgumentNotValidException ex, WebRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation Error",
                VALIDATION_ERROR_TYPE,
                detail,
                request,
                problem -> {});
    }

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatus status,
            String title,
            URI type,
            String detail,
            WebRequest request,
            java.util.function.Consumer<ProblemDetail> customizer) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(type);
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        customizer.accept(problemDetail);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
