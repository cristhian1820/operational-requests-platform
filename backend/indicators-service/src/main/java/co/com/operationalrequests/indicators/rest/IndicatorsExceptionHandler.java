package co.com.operationalrequests.indicators.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class IndicatorsExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalid(IllegalArgumentException e, HttpServletRequest r) {
        var p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        p.setTitle("Rango inválido");
        p.setType(java.net.URI.create("urn:problem:operational-requests:invalid-range"));
        p.setInstance(java.net.URI.create(r.getRequestURI()));
        p.setProperty("errorCode", "INVALID_DATE_RANGE");
        p.setProperty("timestamp", Instant.now());
        return p;
    }
}
