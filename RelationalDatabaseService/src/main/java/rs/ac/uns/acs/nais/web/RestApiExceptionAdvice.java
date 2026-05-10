package rs.ac.uns.acs.nais.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Jedinstven odgovor sa {@code message} da frontend prikaže jasnu poruku (bez sirovog JSON-a).
 */
@RestControllerAdvice
public class RestApiExceptionAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest req) {
        HttpStatusCode code = ex.getStatusCode();
        int sc = code.value();
        HttpStatus status = HttpStatus.resolve(sc);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            sc = status.value();
        }
        String msg = ex.getReason();
        if (msg == null || msg.isBlank()) {
            msg = status.getReasonPhrase();
        }
        return ResponseEntity.status(code).body(errorBody(req, sc, status.name(), msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        String msg =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .collect(Collectors.joining("; "));
        if (msg.isBlank()) {
            msg = "Neispravan unos.";
        }
        Map<String, Object> body =
                errorBody(req, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), msg);
        return ResponseEntity.badRequest().body(body);
    }

    private static Map<String, Object> errorBody(
            HttpServletRequest req, int status, String errorCode, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", errorCode);
        body.put("message", message);
        body.put("path", req.getRequestURI());
        return body;
    }
}
