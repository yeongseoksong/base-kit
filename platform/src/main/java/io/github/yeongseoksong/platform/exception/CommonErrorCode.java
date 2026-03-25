package io.github.yeongseoksong.platform.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_INPUT(400, "C001", "Invalid input value"),
    ENTITY_NOT_FOUND(404, "C002", "Entity not found"),
    METHOD_NOT_ALLOWED(405, "C003", "Method not allowed"),
    UNAUTHORIZED(401, "C004", "Unauthorized"),
    FORBIDDEN(403, "C005", "Access denied"),
    INTERNAL_SERVER_ERROR(500, "C006", "Internal server error");

    private final int status;
    private final String code;
    private final String message;
}
