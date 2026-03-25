package io.github.yeongseoksong.demo.exception;

import io.github.yeongseoksong.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DemoErrorCode implements ErrorCode {

    HELLO_NOT_FOUND(404, "D001", "Hello not found"),
    INVALID_NAME(400, "D002", "Name must not be blank");

    private final int status;
    private final String code;
    private final String message;
}
