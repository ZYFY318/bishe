package com.example.houduan.pojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor  // 自动生成包含所有字段的构造函数
public class ResponseMessage<T> {
    private Integer code;
    private String message;
    private String errorMessage; // 错误信息字段
    private T data;

    // 成功的响应方法
    public static <T> ResponseMessage<T> success(T data) {
        return new ResponseMessage<>(HttpStatus.OK.value(), "success!", null, data);
    }

    public static <T> ResponseMessage<T> success() {
        return new ResponseMessage<>(HttpStatus.OK.value(), "success!", null, null);
    }

    // 错误的响应方法
    public static <T> ResponseMessage<T> error(Integer code, String message, String errorMessage) {
        return new ResponseMessage<>(code, message, errorMessage, null);
    }
}
