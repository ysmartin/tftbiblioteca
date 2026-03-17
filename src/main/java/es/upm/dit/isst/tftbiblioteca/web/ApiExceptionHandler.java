package es.upm.dit.isst.tftbiblioteca.web;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.validation.ConstraintViolationException;

/**
 * Manejo global de errores de entrada para la API manual. Centraliza la
 * traducción de validaciones y cuerpos mal formados en una respuesta 400
 * legible por el cliente, sin interferir con excepciones que ya traen su
 * propio estado HTTP (como {@link org.springframework.web.server.ResponseStatusException}).
 */
@RestControllerAdvice // intercepta excepciones de todos los controladores REST
public class ApiExceptionHandler {

    /**
     * Captura errores de validación de cuerpo ({@link MethodArgumentNotValidException},
     * {@link BindException}), de parámetros de método ({@link ConstraintViolationException},
     * {@link HandlerMethodValidationException}) y de deserialización
     * ({@link HttpMessageNotReadableException}), y devuelve un 400 con un mensaje legible.
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,   // @Valid sobre @RequestBody
            BindException.class,                     // binding fallido (superclase de la anterior)
            ConstraintViolationException.class,      // @Validated en parámetros de método
            HandlerMethodValidationException.class,  // validación de método en Spring 6+
            HttpMessageNotReadableException.class }) // JSON mal formado o tipo incompatible
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        return ResponseEntity.badRequest().body(resolveMessage(ex));
    }

    private String resolveMessage(Exception ex) {
        // BindException y su subclase MethodArgumentNotValidException exponen FieldErrors
        if (ex instanceof BindException bindException) {
            FieldError fieldError = bindException.getBindingResult().getFieldError();
            return fieldError != null
                    ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                    : "Solicitud invalida";
        }
        return ex.getMessage() != null ? ex.getMessage() : "Solicitud invalida";
    }
}
