package net.nature.blog.controller.user;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/**
 * 用户管理
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserApi {

    @Autowired
    private IUserService userService;
    /**
     * 初始化管理员账户
     * @param natureUser
     * @return
     */
    @PostMapping("/init_admin")
    public ResponseResult initAdminAccount(@RequestBody NatureUser natureUser){
        return userService.initAdminAccount(natureUser);
    }


    /**
     * 注册用户
     * @param natureUser
     * @param captchaCode 人类验证码
     * @param verifyCode 邮箱验证码
     * @param captchaKey 人类验证码redis的key
     * @return
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestBody NatureUser natureUser,
                                   @RequestParam("captcha_code")String captchaCode,
                                   @RequestParam("verify_code")String verifyCode,
                                   @RequestParam("captcha_key")String captchaKey){
        return userService.registerUser(natureUser, captchaCode, verifyCode, captchaKey);
    }

    /**
     * 用户登录
     * @param captcha 人类验证码
     * @param captchaKey 取redis的人类验证码所需key值
     * @param natureUser 封装登录信息的bean类
     * @return
     */
    @PostMapping ("/login/{captcha}/{captcha_key}")
    public ResponseResult login(@PathVariable("captcha")String captcha,
                                @PathVariable("captcha_key")String captchaKey,
                                @RequestParam(value = "from", required = false)String from,
                                @RequestBody NatureUser natureUser){
        return userService.doLogin(natureUser, captcha, captchaKey, from);
    }

    /**
     * 获取人类验证码
     * @return
     */
    @GetMapping("/captcha")
    public void getCaptcha(@RequestParam("captcha_key")String captchaKey) {
        try {
            userService.createCaptcha(captchaKey);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     * 发送邮件email
     * @return
     */
    @GetMapping("/verify_code")
    public ResponseResult sendVerifyCode(
                                         @RequestParam("email_address")String emailAddress,
                                         @RequestParam("type")String type){
        return userService.sendEmail(emailAddress, type);
    }

    /**
     * 修改用户密码 找回密码
     * 1.前端请求发送邮箱验证码
     * 2.前端上传验证码and邮箱地址和修改后的密码
     * 3.后端通过邮箱地址在redis中查询邮箱验证码
     * 4.对比用户上传的验证码与redis中该邮箱的正确验证码
     * 5.正确的话，将用户上传的密码，更改进该邮箱（唯一标识）的记录中，完成修改
     * 修改密码，与找回密码都可以，不用userid，不用鉴权。
     * @return
     */
    @PutMapping("/password/{verify_code}")
    public ResponseResult updatePassword(@PathVariable("verify_code")String verifyCode, @RequestBody NatureUser natureUser){
        return userService.updatePassword(verifyCode, natureUser);
    }

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/user_info/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId ){
        return userService.queryUserInfo(userId);
    }

    /**
     * 修改用户信息
     * @return
     */
    @PutMapping({"/user_info/{userId}"})
    public ResponseResult updateUserInfo(@PathVariable("userId") String userId, @RequestBody NatureUser natureUser){
        return userService.updateUserInfo(userId, natureUser);
    }

    /**
     * 删除用户信息
     * @param userId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/user_info/{userId}")
    public ResponseResult deleteUser(@PathVariable("userId") String userId){
        return userService.deleteUser(userId);
    }



    /**
     * 获取全部用户信息
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listUsers(@PathVariable("page")int page, @PathVariable("size")int size,
                                    @RequestParam(value = "userName", required = false)String userName,
                                    @RequestParam(value = "email",required = false)String email){
        return userService.listUsers(page, size, userName, email);
    }

    @PutMapping("/email")
    public ResponseResult updateUserEmail(@RequestParam("email")String email, @RequestParam("verify_code")String verifyCode){
        return userService.updateUserEmail(email, verifyCode);
    }

    /**
     * 退出登录
     * @return
     */
    @DeleteMapping("/logout")
    public ResponseResult logout(){
        return userService.doLogout();
    }

    /**
     * 检查邮箱是否被注册
     * @param email email_address
     * @return
     *
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "邮箱已被注册。"),
            @ApiResponse(code = 40000, message = "邮箱未被注册。")
    })
    @GetMapping("/email")
    public ResponseResult checkEmail(@RequestParam("email")String email){
        return userService.checkEmail(email);
    }

    /**
     * 检查用户名是否被注册
     * @param userName user_name
     * @return
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "用户名已被注册。"),
            @ApiResponse(code = 40000, message = "用户名未被注册。")
    })
    @GetMapping("/user_name")
    public ResponseResult checkUserName(@RequestParam("user_name")String userName){
        return userService.checkUserName(userName);
    }

    @GetMapping("/parse_token")
    public ResponseResult parseToken(){
        return userService.parseToken();
    }

    @PutMapping("/state/{userId}")
    public ResponseResult refreshUser(@PathVariable("userId")String userId){
        return userService.refreshUser(userId);
    }

    @PutMapping("/password")
    public ResponseResult resetPassword(@RequestParam("userId")String userId, @RequestParam("password")String password){
        return userService.resetPassword(userId, password);
    }

    @GetMapping("/count")
    public ResponseResult getUserCount(){
        return userService.getUserCount();
    }
}
