package udtale.config.exceptions;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ProblemDetail base(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status.value());
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setType(URI.create("about:blank")); // replace with docs URL if you have
        pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }

    @ExceptionHandler(UdtaleException.class)
    public ResponseEntity<ProblemDetail> handleUdtaleExceptions(UdtaleException exception, HttpServletRequest request) {

        String name = exception.getClass().getSimpleName();
        String sessionId = request.getSession().getId();

        // create problem detail for the error
        ProblemDetail problemDetail = this.base (exception.getHttpStatus(), name, exception.getMessage());
        problemDetail.setProperty("code", exception.getCode());

        // check if it's a validation error then add more details to the error
        if (exception instanceof ValidationException validationException) {
            problemDetail.setProperty("errors", validationException.getFieldErrors());
        }

        log.info("[{}]  Handled {} code={} status={} path={} cid={}",
                sessionId, name, exception.getCode(), exception.getHttpStatus(), request.getRequestURI(), MDC.get("correlationId"));

        return ResponseEntity.status(exception.getHttpStatus()).body(problemDetail);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateEmailException(EmailAlreadyExistsException exception, HttpServletRequest request){
        String sessionId = request.getSession().getId();
        String name = exception.getClass().getSimpleName();

        ProblemDetail problemDetail = this.base(exception.getHttpStatus(), name, exception.getMessage());
        problemDetail.setProperty("code", exception.getCode());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);

    }


    @ExceptionHandler(TranscriptionException.class)
    public ResponseEntity<ProblemDetail> handleAudioTranscriptionException(TranscriptionException exception, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String name = exception.getClass().getSimpleName();

        ProblemDetail problemDetail = this.base(exception.getHttpStatus(), name, exception.getMessage());
        problemDetail.setProperty("code", exception.getCode());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(AudioProcessingException.class)
    public ResponseEntity<ProblemDetail> handleAudioTranscriptionException(AudioProcessingException exception, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String name = exception.getClass().getSimpleName();

        ProblemDetail problemDetail = this.base(exception.getHttpStatus(), name, exception.getMessage());
        problemDetail.setProperty("code", exception.getCode());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String sessionId = request.getSession().getId();

        Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,
                dfm -> dfm.getDefaultMessage(), (a,b) -> a));

        ProblemDetail problemDetail = base(HttpStatus.CONFLICT, "validation_exception", "one or more fields are invalid");
        problemDetail.setProperty("code", "validation_failed");
        problemDetail.setProperty("errors", errors);

        log.info("[{}]  Handled {} code={} status={} path={} cid={}",
                sessionId, exception.getClass().getSimpleName(), exception.getStatusCode(), exception.getStatusCode(), request.getRequestURI(), MDC.get("correlationId"));

        return ResponseEntity.unprocessableEntity().body(problemDetail);

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraint(ConstraintViolationException exception, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
//        get list of errors in the constraints violations
        Map<String, String > errors = exception.getConstraintViolations().stream().collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (a,b) -> a ));


        ProblemDetail problemDetail = this.base(HttpStatus.CONFLICT, "validation_exception", "one or more parameters are invalid");
        problemDetail.setProperty("code", "validation_failed");
        problemDetail.setProperty("errors", errors);


        log.info("[{}]  Handled {} code={} status={} path={} cid={}",
                sessionId, exception.getClass().getSimpleName(), "contraints_validation_failed", HttpStatus.CONFLICT.value(), request.getRequestURI(), MDC.get("correlationId"));

        return ResponseEntity.unprocessableEntity().body(problemDetail);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception exception, HttpServletRequest request) {
        // build problem detail up here
        ProblemDetail problemDetail = this.base(HttpStatus.BAD_REQUEST, exception.getClass().getSimpleName(), exception.getMessage());
        problemDetail.setProperty("code", "bad_request");

        return ResponseEntity.badRequest().body(problemDetail);
    }


    @ExceptionHandler({ ResponseStatusException.class, ErrorResponseException.class})
    public ResponseEntity<ProblemDetail> handleStatusExceptions(RuntimeException exception, HttpServletRequest request) {

        HttpStatus status = (exception instanceof ResponseStatusException rse ?  HttpStatus.valueOf(rse.getStatusCode().value()) : HttpStatus.INTERNAL_SERVER_ERROR);

        // build our problem detail object from this end
        ProblemDetail problemDetail = this.base(status, status.getClass().getSimpleName(), exception.getMessage());
        problemDetail.setProperty("code", "generic_status_exception");
        return ResponseEntity.status(status).body(problemDetail);
    }

    // "Not found" when *you* throw it (prefer your own ResourceNotFoundException)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.NOT_FOUND, "ResourceNotFoundException", ex.getMessage());
        pd.setProperty("code", "RESOURCE_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnknown(Exception exception, HttpServletRequest request) {
        String cid = MDC.get("correlationId");
        String sessionId = request.getSession().getId();

        log.error("[{}] Unhandled exception at {} cid={}", sessionId, request.getRequestURI(), cid, exception);

        ProblemDetail problemDetail = base(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", "An unexpected error occurred.");
        problemDetail.setProperty("code", "internal_server_error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

//    handling auth exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(Exception exception){
        ProblemDetail pd = base(HttpStatus.FORBIDDEN, "access_denied", "You do not have permission to perform this action.");
        pd.setProperty("code", "FORBIDDEN");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleUnauth(Exception exception) {
        ProblemDetail problemDetail = base(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required.");
        problemDetail.setProperty("code", "UNAUTHORISED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }




}
