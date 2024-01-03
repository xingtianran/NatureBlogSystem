package net.nature.blog.response;

public class ResponseResult {
    private boolean success;
    private int code;
    private String message;
    private Object data;

    public static  ResponseResult SUCCESS(){
        return new ResponseResult(ResponseState.SUCCESS);
    }

    public static ResponseResult GET(ResponseState responseState){
        return new ResponseResult(responseState);
    }
    public static ResponseResult SUCCESS(String message){
        ResponseResult responseResult = new ResponseResult(ResponseState.SUCCESS);
        responseResult.setMessage(message);
        return responseResult;
    }
    public static  ResponseResult FAILURE(){
        return new ResponseResult(ResponseState.FAILURE);
    }
    public static  ResponseResult FAILURE(String message){
        ResponseResult responseResult = new ResponseResult(ResponseState.FAILURE);
        responseResult.setMessage(message);
        return responseResult;
    }
    public ResponseResult(ResponseState responseState) {
        this.success = responseState.isSuccess();
        this.code = responseState.getCode();
        this.message = responseState.getMessage();
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

    public Object getData() {
        return data;
    }

    public ResponseResult setData(Object data) {
        this.data = data;
        return this;
    }
}
