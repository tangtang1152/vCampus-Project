package com.vCampus.net.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用的 Socket 响应对象（可序列化）。
 */
public class SocketResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Serializable data;

    public SocketResponse(boolean success, String message) {
        this(success, message, null);
    }

    public SocketResponse(boolean success, String message, Serializable data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Serializable getData() {
        return data;
    }
}


