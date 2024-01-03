package net.nature.blog.response;

public enum ResponseState {

    SUCCESS(true, 20000, "操作成功"),
    REGISTER_SUCCESS(true, 20001, "注册成功"),

    LOGIN_SUCCESS(true, 20002, "登录成功"),
    FAILURE(false, 40000, "操作失败"),
    ACCOUNT_NOT_LOGIN(false, 40001, "账号未登录"),
    PERMISSION_DENY(false, 40002, "无权访问"),

    ACCOUNT_DENY(false, 40003, "账户已禁用"),
    ERROR_403(false, 40004, "权限不足"),
    ERROR_404(false, 40005, "页面丢失"),
    ERROR_504(false, 40006, "系统繁忙，请稍后重试"),
    ERROR_505(false, 40007, "请求错误，请检查提交数据");

    private boolean success;
    private int code;
    private String message;

    ResponseState(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
