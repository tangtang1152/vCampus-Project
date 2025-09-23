package com.vCampus.net.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用的 Socket 请求对象（可序列化）。
 * 使用 action 指定动作，例如：PING、CHOOSE、BORROW、RENEW、RETURN、RESERVE。
 * 参数以键值对方式携带，保持客户端与服务端一致的 DTO 类定义。
 */
public class SocketRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String action;
    private final Map<String, String> params;
    private final Serializable payload;

    public SocketRequest(String action) {
        this(action, new HashMap<>(), null);
    }

    public SocketRequest(String action, Map<String, String> params) {
        this(action, params, null);
    }

    public SocketRequest(String action, Map<String, String> params, Serializable payload) {
        this.action = action;
        this.params = (params == null) ? new HashMap<>() : new HashMap<>(params);
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }

    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public String getParam(String key) {
        return params.get(key);
    }

    public SocketRequest put(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    public Serializable getPayload() {
        return payload;
    }
}


