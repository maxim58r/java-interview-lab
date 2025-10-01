package ru.max.test.test6;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerDomain {

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ProblemDetail> handle(DomainException ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatus(ex.status());
        pd.setTitle("Domain error");
        pd.setDetail(ex.getMessage());
        pd.setProperty("code", ex.code());
        pd.setProperty("instance", req.getRequestURI());
        ex.props().forEach(pd::setProperty);
        return ResponseEntity.status(ex.status()).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
    }
}