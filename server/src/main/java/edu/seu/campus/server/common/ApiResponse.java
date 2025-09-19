package edu.seu.campus.server.common;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private long ts;
    private String traceId;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 0;
        r.message = "OK";
        r.data = data;
        r.ts = System.currentTimeMillis();
        return r;
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.message = msg;
        r.ts = System.currentTimeMillis();
        return r;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public long getTs() { return ts; }
    public void setTs(long ts) { this.ts = ts; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}


