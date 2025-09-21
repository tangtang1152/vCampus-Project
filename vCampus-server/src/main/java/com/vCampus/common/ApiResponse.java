package com.vCampus.common;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public ApiResponse() {}
    public ApiResponse(int code, String message, T data) {
        this.code = code; this.message = message; this.data = data;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(0, "OK", data); }
    public static <T> ApiResponse<T> ok(String msg, T data) { return new ApiResponse<>(0, msg, data); }
    public static <T> ApiResponse<T> error(String msg) { return new ApiResponse<>(-1, msg, null); }
}



