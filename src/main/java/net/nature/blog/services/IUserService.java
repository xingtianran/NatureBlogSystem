package net.nature.blog.services;

import net.nature.blog.pojo.NatureUser;
import net.nature.blog.response.ResponseResult;

public interface IUserService {
    ResponseResult initAdminAccount(NatureUser natureUser);

    void createCaptcha(String captchaKey) throws Exception;

    ResponseResult sendEmail(String emailAddress, String type);

    ResponseResult registerUser(NatureUser natureUser, String captchaCode, String verifyCode, String captchaKey);

    ResponseResult doLogin(NatureUser natureUser, String captcha, String captchaKey, String from);

    // 1.从cookie拿到tokenKey
    // 2.parseTokenKey，从redis中拿到token，解析token，过期与redis中没有都为null
    // 3.token不行了，就安排refreshToken，如果为空，或者过期都null回去
    // 4.如果refreshToken可以就创建新token和refreshToken
    // 5.通过refreshToken userId查询到user
    // 6.删除原来的refreshToken
    // 7.通过user创建新token和refreshToken
    // 8.返回解析的user。
    NatureUser checkNatureUser();

    ResponseResult queryUserInfo(String userId);

    ResponseResult checkEmail(String email);

    ResponseResult checkUserName(String userName);

    ResponseResult updateUserInfo(String userId, NatureUser natureUser);

    ResponseResult deleteUser(String userId);

    ResponseResult listUsers(int page, int size, String userName, String email);

    ResponseResult updatePassword(String verifyCode, NatureUser natureUser);

    ResponseResult updateUserEmail(String email, String verifyCode);

    ResponseResult doLogout();

    ResponseResult parseToken();

    ResponseResult refreshUser(String userId);

    ResponseResult resetPassword(String userId, String password);

    ResponseResult getUserCount();
}
