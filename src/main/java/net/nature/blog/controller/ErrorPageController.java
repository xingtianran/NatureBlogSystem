package net.nature.blog.controller;

import net.nature.blog.response.ResponseResult;
import net.nature.blog.response.ResponseState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一返回结果
 */
@RestController
public class ErrorPageController {

    @GetMapping("/403")
    public ResponseResult page403(){
        return ResponseResult.GET(ResponseState.ERROR_403);
    }

    @GetMapping("/404")
    public ResponseResult page404(){
        return ResponseResult.GET(ResponseState.ERROR_404);
    }

    @GetMapping("/504")
    public ResponseResult page504(){
        return ResponseResult.GET(ResponseState.ERROR_504);
    }

    @GetMapping("/505")
    public ResponseResult page505(){
        return ResponseResult.GET(ResponseState.ERROR_505);
    }

}
