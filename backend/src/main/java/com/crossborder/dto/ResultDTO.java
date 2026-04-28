package com.crossborder.dto;

public class ResultDTO<T> {

    private Integer code;
    private String message;
    private T data;

    public ResultDTO() {
    }

    public ResultDTO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResultDTO<T> success(T data) {
        return new ResultDTO<>(200, "操作成功", data);
    }

    public static <T> ResultDTO<T> success(String message, T data) {
        return new ResultDTO<>(200, message, data);
    }

    public static <T> ResultDTO<T> error(String message) {
        return new ResultDTO<>(500, message, null);
    }

    public static <T> ResultDTO<T> error(Integer code, String message) {
        return new ResultDTO<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
