package com.spring.eventhub.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.spring.eventhub.bookings.exception.NotAvailiableCapacityException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

@RestControllerAdvice
public class GlobalExceptionHandler {
     private static  final String TIMEOUT_ERROR_MESSAGE = "Timeout";
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Tipo de Java -> frase para el cliente. Los enums no están aquí: se tratan en describeType().
    private static final Map<Class<?>, String> TYPE_DESCRIPTIONS = Map.ofEntries(
            Map.entry(Integer.class, "an integer"),
            Map.entry(int.class, "an integer"),
            Map.entry(Long.class, "an integer"),
            Map.entry(long.class, "an integer"),
            Map.entry(BigDecimal.class, "a number"),
            Map.entry(Boolean.class, "true or false"),
            Map.entry(boolean.class, "true or false"),
            Map.entry(String.class, "a text"),
            Map.entry(UUID.class, "a valid UUID"),
            Map.entry(List.class, "a comma-separated list of valid values"),
            Map.entry(Instant.class, "a valid ISO date"));

    private static final String DEFAULT_TYPE_DESCRIPTION = "a valid value";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> resourceNotFoundHandler(NotFoundException ex) {
        return buildError(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> validationErrorHandler(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField).thenComparing(FieldError::getDefaultMessage))
                .map(FieldError::getDefaultMessage)
                .toList();
        ValidationErrorResponse error = new ValidationErrorResponse(errors, status.getReasonPhrase(), status.value());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> bodyErrorHandler(HttpMessageNotReadableException ex) {
        return buildError(selectBodyMessage(ex.getCause()), HttpStatus.BAD_REQUEST);
    }

    // La causa es una excepción de Jackson. El orden importa: UnrecognizedPropertyException
    // desciende de MismatchedInputException, así que va primero.
    private String selectBodyMessage(Throwable cause) {
        if (cause == null) {
            return "Request body is required";
        }
        if (cause instanceof UnrecognizedPropertyException unrecognized) {
            return "property " + unrecognized.getPropertyName() + " should not exist";
        }
        if (cause instanceof MismatchedInputException mismatched) {
            String field = fieldPath(mismatched);
            if (field.isEmpty()) {
                return "Request body is malformed";
            }
            return field + " must be " + describeType(mismatched.getTargetType());
        }
        if (cause instanceof JacksonException) {
            return "Malformed JSON request";
        }
        return "Request body is malformed";
    }

    private String fieldPath(JacksonException ex) {
        return ex.getPath().stream()
                .map(JacksonException.Reference::getPropertyName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
    }

    private String describeType(Class<?> type) {
        if (type == null) {
            return DEFAULT_TYPE_DESCRIPTION;
        }
        if (type.isEnum()) {
            String values = Arrays.stream(type.getEnumConstants())
                    .map(constant -> ((Enum<?>) constant).name())
                    .collect(Collectors.joining(", "));
            return "one of the following values: " + values;
        }
        return TYPE_DESCRIPTIONS.getOrDefault(type, DEFAULT_TYPE_DESCRIPTION);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> mediaTypeErrorHandler(HttpMediaTypeNotSupportedException ex) {
        return buildError("Media type must be application/json", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> methodTypeErrorHandler(MethodArgumentTypeMismatchException ex) {
        String message = ex.getName() + " must be " + describeType(ex.getRequiredType());
        return buildError(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> missingParameterHandler(MissingServletRequestParameterException ex) {
        return buildError(ex.getParameterName() + " is required", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> pathNotFoundErrorHandler(NoResourceFoundException ex) {
        return buildError("Path /" + ex.getResourcePath() + " does not exist", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> methodNotAllowedHandler(HttpRequestMethodNotSupportedException ex) {
        return buildError("Method " + ex.getMethod() + " is not allowed", HttpStatus.METHOD_NOT_ALLOWED,
                ex.getHeaders());
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handlerMethodValidation(HandlerMethodValidationException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<String> errors = ex.getAllErrors().stream().map(error -> error.getDefaultMessage()).toList();
         ValidationErrorResponse error = new ValidationErrorResponse(errors, status.getReasonPhrase(), status.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

        @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> accessDeniedHandler(AccessDeniedException  ex){
     
        return  buildError("User does not have permissions", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotAvailiableCapacityException.class)
    public ResponseEntity<ApiErrorResponse> noAvailiableCapacity(NotAvailiableCapacityException ex){
        return buildError(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> authorizationHandler(AuthorizationDeniedException ex){
     
        return  buildError("User does not have permissions", HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler (InterruptedException.class)
    public ResponseEntity<ApiErrorResponse> timeoutHandler(InterruptedException ex){
        return buildError(TIMEOUT_ERROR_MESSAGE, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler (AsyncRequestTimeoutException.class)
    public ResponseEntity<ApiErrorResponse> asynctimeoutHandler(AsyncRequestTimeoutException ex){
        return buildError(TIMEOUT_ERROR_MESSAGE, HttpStatus.REQUEST_TIMEOUT);
    }


    

    // Último recurso: el cliente nunca ve el mensaje de la excepción, pero el servidor sí lo registra.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> generalErrorHandler(Exception ex) {
        log.error("Unexpected error", ex);
        return buildError("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> buildError(String message, HttpStatus status) {
        ApiErrorResponse error = new ApiErrorResponse(message, status.getReasonPhrase(), status.value());
        return new ResponseEntity<>(error, status);
    }

    private ResponseEntity<ApiErrorResponse> buildError(String message, HttpStatus status, HttpHeaders headers) {
        ApiErrorResponse error = new ApiErrorResponse(message, status.getReasonPhrase(), status.value());
        return ResponseEntity.status(status).headers(headers).body(error);
    }

}
