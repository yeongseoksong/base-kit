package io.github.yeongseoksong.platform.exception;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}
