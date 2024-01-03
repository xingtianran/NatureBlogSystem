package net.nature.blog.interceptor;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.CookieUtils;
import net.nature.blog.utils.RedisUtil;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Slf4j
@Component
public class ApiInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Gson gson;

    /**
     * 方法执行前，true放行，false拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取方法上的注解
            CheckTooFrequentCommit methodAnnotation = handlerMethod.getMethodAnnotation(CheckTooFrequentCommit.class);
            if (methodAnnotation != null){
                String methodName = handlerMethod.getMethod().getName();
                String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
                // 查看redis
                String commitSign = (String) redisUtil.get(Constants.User.KEY_COMMIT_SIGN + tokenKey + methodName);
                if (!TextUtils.isEmpty(commitSign)){
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    ResponseResult result = ResponseResult.FAILURE("提交频繁，请稍后重试");
                    PrintWriter writer = response.getWriter();
                    writer.write(gson.toJson(result));
                    writer.flush();
                    return false;
                }else {
                    redisUtil.set(Constants.User.KEY_COMMIT_SIGN + tokenKey + methodName, "true",Constants.TimeValueInSecond.SECOND * 5);
                }
            }
        }
        return true;
    }
}
