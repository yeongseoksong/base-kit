package io.github.yeongseoksong.platform.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private String code;
    private String message;
    private List<FieldError> errors;

    public static ErrorResponse of(String code, String message) {
        ErrorResponse response = new ErrorResponse();
        response.code = code;
        response.message = message;
        response.errors = List.of();
        return response;
    }

    public static ErrorResponse of(String code, String message, BindingResult bindingResult) {
        ErrorResponse response = new ErrorResponse();
        response.code = code;
        response.message = message;
        response.errors = FieldError.of(bindingResult);
        return response;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        private static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> {
                        FieldError fieldError = new FieldError();
                        fieldError.field = error.getField();
                        fieldError.value = error.getRejectedValue() == null
                                ? "" : error.getRejectedValue().toString();
                        fieldError.reason = error.getDefaultMessage();
                        return fieldError;
                    })
                    .collect(Collectors.toList());
        }
    }
}
