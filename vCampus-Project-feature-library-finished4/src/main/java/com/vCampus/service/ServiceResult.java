package com.vCampus.service;

public class ServiceResult {
    private final boolean success;
    private final String message;

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public static ServiceResult ok(String msg) { return new ServiceResult(true, msg); }
    public static ServiceResult ok() { return new ServiceResult(true, "OK"); }
    public static ServiceResult fail(String msg) { return new ServiceResult(false, msg); }
}


