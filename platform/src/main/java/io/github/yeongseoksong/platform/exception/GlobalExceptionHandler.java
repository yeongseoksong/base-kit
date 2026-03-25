package io.github.yeongseoksong.platform.exception;

import io.github.yeongseoksong.platform.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리 핸들러.
 * 애플리케이션 내에서 발생하는 주요 예외를 가로채어 공통된 {@link ErrorResponse} 형식으로 응답합니다.
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 발생하는 {@link BusinessException}을 처리합니다.
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode.getCode(), e.getMessage()));
    }

    /**
     * @Valid 또는 @Validated 바인딩 오류(주로 DTO 검증)가 발생할 때 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        CommonErrorCode.INVALID_INPUT.getCode(),
                        CommonErrorCode.INVALID_INPUT.getMessage(),
                        e.getBindingResult()));
    }

    /**
     * 모델 바인딩 시 발생하는 {@link BindException}을 처리합니다.
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.error("BindException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        CommonErrorCode.INVALID_INPUT.getCode(),
                        CommonErrorCode.INVALID_INPUT.getMessage(),
                        e.getBindingResult()));
    }

    /**
     * 지원하지 않는 HTTP 메서드로 요청이 들어올 경우 처리합니다.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of(
                        CommonErrorCode.METHOD_NOT_ALLOWED.getCode(),
                        CommonErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    /**
     * 메서드 인자의 타입이 일치하지 않을 때 발생합니다. (예: PathVariable의 타입 오류)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        CommonErrorCode.INVALID_INPUT.getCode(),
                        CommonErrorCode.INVALID_INPUT.getMessage()));
    }

    /**
     * 도메인 로직의 유효성 검증 실패({@link IllegalArgumentException})나
     * 잘못된 상태 호출({@link IllegalStateException})을 처리합니다.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    protected ResponseEntity<ErrorResponse> handleBadRequestException(RuntimeException e) {
        log.error("Bad Request Exception: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        CommonErrorCode.INVALID_INPUT.getCode(),
                        e.getMessage()));
    }

    /**
     * 정의되지 않은 모든 예외를 최종적으로 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
