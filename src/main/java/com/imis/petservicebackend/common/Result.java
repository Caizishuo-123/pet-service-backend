package com.imis.petservicebackend.common;

import lombok.Data;
import lombok.ToString;

/**
 * 通用返回类
 * @param <T> 返回数据类型
 */
@Data
@ToString
public class Result<T> {

    /**
     * 返回的状态码
     */
    private Integer code;

    /**
     * 返回的消息
     */
    private String message;

    /**
     * 返回的数据
     */
    private T data;

    /**
     * 构造方法
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功
     * @param data 数据
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功
     * @param data 数据
     * @param message 信息
     * @return 成功结果
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultStatus.SUCCESS.getCode(), message, data);
    }

    /**
     * 成功，自定义状态
     * @param resultStatus 自定义状态
     * @param data 数据
     * @return 成功结果
     */
    public static <T> Result<T> success(ResultStatus resultStatus, T data) {
        return new Result<>(resultStatus.getCode(), resultStatus.getMessage(), data);
    }

    /**
     * 成功，自定义状态码、消息和数据
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @return 成功结果
     */
    public static <T> Result<T> success(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 失败
     * @return 失败结果
     */
    public static <T> Result<T> fail() {
        return fail("操作失败");
    }

    /**
     * 失败
     * @param message 信息
     * @return 失败结果
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultStatus.SERVER_ERROR.getCode(), message, null);
    }

    /**
     * 失败
     * @param code 状态码
     * @param message 信息
     * @return 失败结果
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败，自定义状态
     * @param resultStatus 自定义状态
     * @return 失败结果
     */
    public static <T> Result<T> fail(ResultStatus resultStatus) {
        return new Result<>(resultStatus.getCode(), resultStatus.getMessage(), null);
    }

    /**
     * 失败，自定义状态和数据
     * @param resultStatus 自定义状态
     * @param data 数据
     * @return 失败结果
     */
    public static <T> Result<T> fail(ResultStatus resultStatus, T data) {
        return new Result<>(resultStatus.getCode(), resultStatus.getMessage(), data);
    }

    /**
     * 判断操作是否成功
     * @return 如果操作成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return code == ResultStatus.SUCCESS.getCode();
    }
}