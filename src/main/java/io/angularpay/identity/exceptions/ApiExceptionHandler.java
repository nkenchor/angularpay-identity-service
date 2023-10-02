package io.angularpay.identity.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.angularpay.identity.common.Constants.ERROR_SOURCE;
import static io.angularpay.identity.exceptions.ErrorCode.GENERIC_ERROR;
import static io.angularpay.identity.helpers.Helper.writeAsStringOrDefault;

@SuppressWarnings({"ConstantConditions"})
@ControllerAdvice
@Slf4j
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatus status, WebRequest request) {

        List<ErrorObject> errors = new ArrayList<>();

        log.error("Request :{}", printParameters(request));
        log.error("Http Headers {}", headers);
        log.error("Http Status {}", status);
        log.error("Exception", exception);

        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errors.add(ErrorObject.builder()
                    .code(GENERIC_ERROR)
                    .message(error.getField() + " - " + error.getDefaultMessage())
                    .source(ERROR_SOURCE)
                    .build());
        });

        exception.getBindingResult().getGlobalErrors().forEach(error -> {
            errors.add(ErrorObject.builder()
                    .code(GENERIC_ERROR)
                    .message(error.getObjectName() + " - " + error.getDefaultMessage())
                    .source(ERROR_SOURCE)
                    .build());
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorReference(UUID.randomUUID().toString())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .errors(errors)
                .build();

        log.info("error response: {}", writeAsStringOrDefault(mapper, errorResponse));

        return handleExceptionInternal(exception, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exceptions,
            HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.error("Request :{}", printParameters(request));
        log.error("Http Headers {}", headers);
        log.error("Http Status {}", status);
        log.error("Exception", exceptions);

        List<ErrorObject> errors = Collections.singletonList(ErrorObject.builder()
                .code(GENERIC_ERROR)
                .message(exceptions.getLocalizedMessage())
                .source(ERROR_SOURCE)
                .build());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorReference(UUID.randomUUID().toString())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .errors(errors)
                .build();

        log.info("error response: {}", writeAsStringOrDefault(mapper, errorResponse));

        return handleExceptionInternal(exceptions, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    public ResponseEntity<Object> handleMissingRequestHeaderException(WebRequest request) {

        log.error("Client Request Header missing");

        List<ErrorObject> errors = Collections.singletonList(ErrorObject.builder()
                .code(GENERIC_ERROR)
                .message("Please provide valid Client Request Header")
                .source(ERROR_SOURCE)
                .build());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorReference(UUID.randomUUID().toString())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .errors(errors)
                .build();

        log.info("error response: {}", writeAsStringOrDefault(mapper, errorResponse));

        return handleExceptionInternal(null, errorResponse, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }


    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exceptions, WebRequest request) {

        log.error("Request argument mismatch Exception", exceptions);

        List<ErrorObject> errors = Collections.singletonList(ErrorObject.builder()
                .code(GENERIC_ERROR)
                .message(exceptions.getLocalizedMessage())
                .source(ERROR_SOURCE)
                .build());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorReference(UUID.randomUUID().toString())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .errors(errors)
                .build();

        log.info("error response: {}", writeAsStringOrDefault(mapper, errorResponse));

        return handleExceptionInternal(exceptions, errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exceptions,
            HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.error("Request :{}", printParameters(request));
        log.error("Http Headers {}", headers);
        log.error("Http Status {}", status);
        log.error("Exception", exceptions);

        String message = exceptions.getMethod() +
                " method is not supported. Supported methods are: " +
                Objects.requireNonNull(exceptions.getSupportedHttpMethods()).stream().map(Object::toString).collect(Collectors.joining(","));

        List<ErrorObject> errors = Collections.singletonList(ErrorObject.builder()
                .code(GENERIC_ERROR)
                .message(exceptions.getLocalizedMessage() + " - " + message)
                .source(ERROR_SOURCE)
                .build());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorReference(UUID.randomUUID().toString())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .errors(errors)
                .build();

        log.info("error response: {}", writeAsStringOrDefault(mapper, errorResponse));

        return handleExceptionInternal(exceptions, errorResponse, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exceptions,
            HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.error("Request :{}", printParameters(request));
        log.error("Http Headers {}", headers);
        log.error("Http Status {}", status);
        log.error("Exception", exceptions);

        String message = exceptions.getContentType() +
                " media type is not supported. Supported media types are: " +
                exceptions.getSupportedMediaTypes().stream().map(Object::toString).collect(Collectors.joining(","));

        List<ErrorObject> errors = Collections.singletonList(ErrorObject.builder()
                .code(GENERIC_ERROR)
                .message(exceptions.getLocalizedMessage() + " - " + message)
                .source(ERROR_SOURCE)
                .build());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorReference(UUID.randomUUID().toString())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .errors(errors)
                .build();

        log.info("error response: {}", writeAsStringOrDefault(mapper, errorResponse));

        return handleExceptionInternal(exceptions, errorResponse, new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception exception, WebRequest request) {
        CommandException commandException = exception instanceof CommandException ? ((CommandException) exception) : null;

        log.error("Handle All", exception);

        HttpStatus httpStatus = Objects.nonNull(commandException) ? commandException.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        Throwable cause = Objects.nonNull(commandException) ? commandException.getCause() : exception.getCause();

        ValidationException validationException = cause instanceof ValidationException ?
                ((ValidationException) cause) : null;

        List<ErrorObject> errors = Objects.nonNull(validationException) && !CollectionUtils.isEmpty(validationException.getErrors()) ?
                validationException.getErrors().stream()
                        .map(x -> ErrorObject.builder()
                                .code(x.getCode())
                                .message(x.getMessage())
                                .source(x.getSource())
                                .build())
                        .collect(Collectors.toList()) :
                Objects.nonNull(commandException) ?
                        Collections.singletonList(ErrorObject.builder()
                                .code(commandException.getErrorCode())
                                .message(commandException.getErrorCode().getDefaultMessage())
                                .source(ERROR_SOURCE)
                                .build()) : null;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorReference(UUID.randomUUID().toString())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .errors(errors)
                .build();

        log.info("error response: {}", writeAsStringOrDefault(mapper, errorResponse));

        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), httpStatus, request);
    }

    private String printParameters(WebRequest request) {
        StringBuilder paramsList = new StringBuilder();
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            paramsList.append(entry.getKey());
            paramsList.append(":");
            paramsList.append(Arrays.toString(entry.getValue()));
        }

        return paramsList.toString();
    }

}
