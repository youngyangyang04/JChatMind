package com.kama.jchatmind.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiCode.SUCCESS.code, ApiCode.SUCCESS.message, data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ApiCode.SUCCESS.code, ApiCode.SUCCESS.message, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(ApiCode.SUCCESS.code, message, data);
    }

    public static <T> ApiResponse<T> error(ApiCode code, String message) {
        return new ApiResponse<>(code.getCode(), message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ApiCode.ERROR.getCode(), message, null);
    }

    @Getter
    @AllArgsConstructor
    public enum ApiCode {
        SUCCESS(200, "success"),
        ERROR(500, "error");

        private final int code;
        private final String message;

        public static ApiCode fromCode(int code) {
            for (ApiCode value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid code: " + code);
        }
    }
}
