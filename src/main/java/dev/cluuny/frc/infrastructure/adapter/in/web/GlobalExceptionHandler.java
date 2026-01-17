package dev.cluuny.frc.infrastructure.adapter.in.web;

import dev.cluuny.frc.domain.exception.EmptyStatementException;
import dev.cluuny.frc.domain.exception.InvalidTransactionDataException;
import dev.cluuny.frc.domain.exception.ReconciliationException;
import dev.cluuny.frc.domain.exception.ReportStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmptyStatementException.class)
    public ResponseEntity<Object> handleEmptyStatementException(EmptyStatementException ex, WebRequest request) {
        logger.warn("Empty statement exception: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTransactionDataException.class)
    public ResponseEntity<Object> handleInvalidTransactionDataException(InvalidTransactionDataException ex, WebRequest request) {
        logger.warn("Invalid transaction data: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReportStorageException.class)
    public ResponseEntity<Object> handleReportStorageException(ReportStorageException ex, WebRequest request) {
        logger.error("Report storage error: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ReconciliationException.class)
    public ResponseEntity<Object> handleReconciliationException(ReconciliationException ex, WebRequest request) {
        logger.error("Generic reconciliation error: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, status);
    }
}
