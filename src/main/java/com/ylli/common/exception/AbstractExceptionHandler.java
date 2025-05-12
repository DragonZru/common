package com.ylli.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author ylli
 */
//@RestControllerAdvice
public abstract class AbstractExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractExceptionHandler.class);

    @Value("${exceptionHandler.print.enable:true}")
    boolean printEnable;

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<?> exceptionHandler(GenericException ex) {
        return ResponseEntity.status(ex.getCode()).body(new ResponseBody(ex.getCode(), ex.getMessage(), printEnable ? printStackTrace(ex) : null));
    }

    @ExceptionHandler
    public ResponseEntity<?> exceptionHandler(Exception ex) {
        if (printEnable) {
            logger.error(printStackTrace(ex));
        }
        HttpStatusCode statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();
        if (ex instanceof ErrorResponse) {
            statusCode = ((ErrorResponse) ex).getStatusCode();
            message = ((ErrorResponse) ex).getBody().getDetail();
        }

        return ResponseEntity
                .status(statusCode)
                .body(new ResponseBody(statusCode.value(), message, printEnable ? printStackTrace(ex) : null));
    }

    private String printStackTrace(Exception ex) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            ex.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ResponseBody {
        public int code;
        public String message;
        public String stackTrace;

        public ResponseBody(int code, String message, String stackTrace) {
            this.code = code;
            this.message = message;
            this.stackTrace = stackTrace;
        }

        public ResponseBody(HttpStatus httpStatus, String message, String stackTrace) {
            this.code = httpStatus.value();
            this.message = message;
            this.stackTrace = stackTrace;
        }

        /*
         * debugMsg excluded in json response when debugMsg is null
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getStackTrace() {
            return stackTrace;
        }
    }
}
