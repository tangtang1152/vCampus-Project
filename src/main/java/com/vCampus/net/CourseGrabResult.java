package com.vCampus.net;

/**
 * 选课请求的结果封装
 */
public class CourseGrabResult {
    private final boolean success;
    private final String message;

    public CourseGrabResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}


