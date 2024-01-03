package net.nature.blog.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.mapper.RefreshTokenMapper;
import net.nature.blog.mapper.SettingMapper;
import net.nature.blog.mapper.UserMapper;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.pojo.RefreshToken;
import net.nature.blog.pojo.Setting;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.response.ResponseState;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Transactional
@Service
public class UserServiceImpl extends BaseService implements IUserService {


    @Autowired
    private IdWorker idWorker;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SettingMapper settingMapper;

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public ResponseResult initAdminAccount(NatureUser natureUser) {
        // 检查管理员用户是否已经初始化
        Setting managerAccount = settingMapper.findOneByKey(Constants.Setting.MANAGER_ACCOUNT_STATE);
        if (managerAccount != null){
            return ResponseResult.FAILURE("管理员账号已经初始化。");
        }
        // 检查数据
        if (TextUtils.isEmpty(natureUser.getUserName())){
            return ResponseResult.FAILURE("用户名不能为空。");
        }
        if (TextUtils.isEmpty(natureUser.getPassword())){
            return ResponseResult.FAILURE("密码不能为空。");
        }
        if (TextUtils.isEmpty(natureUser.getEmail())) {
            return ResponseResult.FAILURE("邮箱不能为空。");
        }
        // 补充数据
        natureUser.setId(String.valueOf(idWorker.nextId()));
        natureUser.setRoles(Constants.User.ROLE_ADMIN);
        natureUser.setAvatar(Constants.User.DEFAULT_AVATAR);
        natureUser.setState(Constants.User.DEFAULT_STATE);
        String remoteAddr = getRequest().getRemoteAddr();
        natureUser.setRegIp(remoteAddr);
        natureUser.setLoginIp(remoteAddr);
        natureUser.setCreateTime(new Date());
        natureUser.setUpdateTime(new Date());

        // 密码加密
        String passwordEncode = bCryptPasswordEncoder.encode(natureUser.getPassword());
        natureUser.setPassword(passwordEncode);

        // 保存数据
        userMapper.saveOne(natureUser);

        Setting setting = new Setting();
        setting.setId(String.valueOf(idWorker.nextId()));
        setting.setKey(Constants.Setting.MANAGER_ACCOUNT_STATE);
        setting.setValue("1");
        setting.setCreateTime(new Date());
        setting.setUpdateTime(new Date());
        settingMapper.saveOne(setting);

        return ResponseResult.SUCCESS("初始化成功。");
    }

    @Autowired
    private Random random;

    @Autowired
    private RedisUtil redisUtil;

    // 全部字体类型
    public static final int[] CAPTCHA_FRONT_TYPE = {
            Captcha.FONT_1,
            Captcha.FONT_2,
            Captcha.FONT_3,
            Captcha.FONT_4,
            Captcha.FONT_5,
            Captcha.FONT_6,
            Captcha.FONT_7,
            Captcha.FONT_8,
            Captcha.FONT_9,
            Captcha.FONT_10
    };

    // 全部字符类型
    public static final int[] CAPTCHA_CHAR_TYPE = {
            Captcha.TYPE_DEFAULT,
            Captcha.TYPE_ONLY_NUMBER,
            Captcha.TYPE_ONLY_CHAR,
            Captcha.TYPE_ONLY_UPPER,
            Captcha.TYPE_ONLY_LOWER,
            Captcha.TYPE_NUM_AND_UPPER
    };

    /**
     * 发送人类验证码
     * @param captchaKey
     * @throws Exception
     */
    @Override
    public void createCaptcha(String captchaKey) throws Exception {
        // 校验数据
        if (TextUtils.isEmpty(captchaKey) || captchaKey.length() < 13){
            return;
        }
        long key;
        try {
            key = Long.parseLong(captchaKey);
        }catch (Exception e){
            return;
        }
        // 设置人类验证码
        // 设置请求头为输出图片类型
        HttpServletResponse response = getResponse();
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        int width = 250;
        int height = 40;
        // 设置人类验证码类型
        int captchaType = random.nextInt(3);
        Captcha captcha;
        if (captchaType == 0){
            // 普通
            captcha  = new SpecCaptcha(width, height, 5);
        }else if (captchaType == 1){
            // git
            captcha = new GifCaptcha(width, height);
        }else {
            // 算术
            captcha = new ArithmeticCaptcha(width, height);
            captcha.setLen(2);
        }
        // 设置字体
        captcha.setFont(CAPTCHA_FRONT_TYPE[random.nextInt(CAPTCHA_FRONT_TYPE.length)]);  // 有默认字体，可以不用设置
        // 设置字符类型，纯数字、数字字母混合、大写小写混合等
        captcha.setCharType(CAPTCHA_CHAR_TYPE[random.nextInt(CAPTCHA_CHAR_TYPE.length)]);

        log.info("captchaCode ==> " + captcha.text());
        // 将人类验证码存放到redis中
        redisUtil.set(Constants.User.KEY_CAPTCHA_CONTENT + key,captcha.text(),60 * 10);

        // 输出图片流
        captcha.out(response.getOutputStream());

    }

    @Autowired
    private TaskService taskService;

    /**
     *
     * @param emailAddress
     * @param type 判断业务类型
     *             注册（register）：查询邮箱，邮箱如果有就不行，邮箱已经注册。
     *             找回密码（forget）：邮箱如果没有就不行，邮箱不能没有注册
     *             更改邮箱（uodate）：邮箱不能注册。
     * @return
     */
    @Override
    public ResponseResult sendEmail( String emailAddress, String type) {
        if (TextUtils.isEmpty(emailAddress)){
            return ResponseResult.FAILURE("邮箱不能为空。");
        }
        // 判断业务类型
        if ("register".equals(type) || "update".equals(type)){
            NatureUser userDbByEmail = userMapper.findOneByEmail(emailAddress);
            if (userDbByEmail != null){
                return ResponseResult.FAILURE("邮箱已经被注册。");
            }
        }else if ("forget".equals(type)){
            NatureUser userDbByEmail = userMapper.findOneByEmail(emailAddress);
            if (userDbByEmail == null){
                return ResponseResult.FAILURE("邮箱没有被注册。");
            }
        }
        // 1.防止暴力发送，每个邮箱地址发送间隔30s，每个ip间隔1小时内不超过10次
        // 取出ip，特殊字符处理，便于储存redis
        String ipAddr = getRequest().getRemoteAddr();
        if (ipAddr != null){
            ipAddr = ipAddr.replaceAll(":", "_");
        }
        // ip访问次数
        String ipSendTimeStr = (String) redisUtil.get(Constants.User.KEY_EMAIL_SEND_IP + ipAddr);
        Integer ipSendTime = null;
        if (ipSendTimeStr != null){
            ipSendTime = Integer.valueOf(ipSendTimeStr);
        }
        log.info("ipSendTime ==> " + ipSendTime);
        if (ipSendTime != null && ipSendTime > 10){
            return ResponseResult.FAILURE("您发送的验证码太频繁了！ 请一个小时后重试。");
        }
        // email控制
        Object hasEmailAddrTime = redisUtil.get(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress);
        if (hasEmailAddrTime != null){
            return ResponseResult.FAILURE("您发送的验证码太频繁了！请30秒后重试。");
        }
        // 2.校验邮箱地址是否正确
        if (!TextUtils.isEmailAddressOk(emailAddress)) {
            return ResponseResult.FAILURE("邮箱地址不正确。");
        }
        // 3.发送邮箱验证码
        int verifyCode = random.nextInt(999999);
        if (verifyCode < 100000){
            verifyCode += 100000;
        }
        log.info("verifyCode ==> " + verifyCode);
        try {
            taskService.sendVerifyCode(String.valueOf(verifyCode),emailAddress);
        } catch (Exception e) {
            return ResponseResult.FAILURE("邮箱验证码发送失败。");
        }
        // 4.将验证码存储到redis，做ip和email记录
        if (ipSendTime == null){
            ipSendTime = 0;
        }
        ipSendTime++;
        // ip控制
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_IP + ipAddr,String.valueOf(ipSendTime), 60 * 60);
        // email控制，发送过邮箱短信后，30秒后才能第二次
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress,"true",30);
        // 验证码有效10分钟
        redisUtil.set(Constants.User.KEY_EMAIL_CONTENT + emailAddress, String.valueOf(verifyCode), 60 * 10);
        return ResponseResult.SUCCESS("验证码发送成功。");
    }

    @Override
    public ResponseResult registerUser(NatureUser natureUser, String captchaCode, String verifyCode, String captchaKey) {
        // 1.检查数据
        // 用户名
        String userName = natureUser.getUserName();
        if (TextUtils.isEmpty(userName)){
            return ResponseResult.FAILURE("用户名不能为空。");
        }
        NatureUser userDbByUserName = userMapper.findOneByUserName(userName);
        if (userDbByUserName != null){
            return ResponseResult.FAILURE("用户名已经注册。");
        }
        // 邮箱地址
        String email = natureUser.getEmail();
        if (TextUtils.isEmpty(email)){
            return ResponseResult.FAILURE("邮箱不能为空。");
        }
        if (!TextUtils.isEmailAddressOk(email)){
            return ResponseResult.FAILURE("邮箱格式不正确。");
        }
        NatureUser userDbByEmail = userMapper.findOneByEmail(email);
        if (userDbByEmail != null){
            return ResponseResult.FAILURE("邮箱已经注册。");
        }

        // 2.比较人类验证码
        if (TextUtils.isEmpty(captchaCode)){
            return ResponseResult.FAILURE("人类验证码不能为空。");
        }
        String storeCaptcha = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (storeCaptcha == null){
            return ResponseResult.FAILURE("人类验证码已过期。");
        }
        if (!storeCaptcha.equalsIgnoreCase(captchaCode)){
            return ResponseResult.FAILURE("人类验证码不正确。");
        }else {
            redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        }

        // 3.比较邮箱验证码
        if (TextUtils.isEmpty(verifyCode)){
            return ResponseResult.FAILURE("邮箱验证码不能为空。");
        }
        String storeVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CONTENT + email);
        if (storeVerifyCode == null){
            return ResponseResult.FAILURE("邮箱验证码已过期。");
        }
        if (!storeVerifyCode.equals(verifyCode)){
            return ResponseResult.FAILURE("邮箱验证码不正确");
        }else {
            redisUtil.del(Constants.User.KEY_EMAIL_CONTENT + email);
        }

        // 4.密码加密，放入对象
        String password = natureUser.getPassword();
        if (TextUtils.isEmpty(password)){
            return ResponseResult.FAILURE("密码不能为空。");
        }
        String encodePassword = bCryptPasswordEncoder.encode(password);
        natureUser.setPassword(encodePassword);
        // 4.补充数据
        natureUser.setAvatar(Constants.User.DEFAULT_AVATAR);
        natureUser.setId(String.valueOf(idWorker.nextId()));
        natureUser.setUpdateTime(new Date());
        natureUser.setCreateTime(new Date());
        natureUser.setRoles(Constants.User.ROLE_NORMAL);
        natureUser.setState(Constants.User.DEFAULT_STATE);
        natureUser.setRegIp(getRequest().getRemoteAddr());
        natureUser.setLoginIp(getRequest().getRemoteAddr());

        // 5.保存到数据库
        userMapper.saveOne(natureUser);
        return ResponseResult.GET(ResponseState.REGISTER_SUCCESS);
    }

    @Override
    public ResponseResult doLogin(NatureUser natureUser,
                                  String captcha, String captchaKey, String from) {
        // 判断from来源，如果是pc端的话，不写，如果是手机端的话，要写
        if (TextUtils.isEmpty(from)){
            from = Constants.User.FROM_PC;
        }
        if (!Constants.User.FROM_PC.equals(from) && !Constants.User.FROM_MOBILE.equals(from)){
            return ResponseResult.FAILURE("不支持该端登录");
        }
        // 1.先检查验证码
        String storeCaptcha = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (storeCaptcha == null){
            return ResponseResult.FAILURE("人类验证码已过期。");
        }
        if (!storeCaptcha.equalsIgnoreCase(captcha)){
            return ResponseResult.FAILURE("人类验证码不正确。");
        }

        // 2.检查数据
        String userName = natureUser.getUserName();
        if (TextUtils.isEmpty(userName)){
            return ResponseResult.FAILURE("账号不能为空。");
        }
        String password = natureUser.getPassword();
        if (TextUtils.isEmpty(password)){
            return ResponseResult.FAILURE("密码不能为空。");
        }

        // 3.比较数据是否正确
        NatureUser userFromDb = userMapper.findOneByUserName(userName);
        if (userFromDb == null){
            userFromDb = userMapper.findOneByEmail(userName);
        }
        if (userFromDb == null){
            log.info("账号错误...");
            return ResponseResult.FAILURE("账号或密码错误。");
        }
        boolean matchesResult = bCryptPasswordEncoder.matches(password, userFromDb.getPassword());
        if (!matchesResult){
            log.info("密码错误...");
            return ResponseResult.FAILURE("账号或密码错误。");
        }
        // 4.登录成功 判断状态
        if (!Constants.User.DEFAULT_STATE.equals(userFromDb.getState())){
            return ResponseResult.GET(ResponseState.ACCOUNT_DENY);
        }
        // 5.删除redis中的验证码
        redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        // 4.更新用户和用户登录id
        userMapper.updateTimeAndIp(userFromDb.getId(), new Date(), getRequest().getRemoteAddr());
        // 6.创建token和refreshToken
        createTokenAndRefreshToken(userFromDb, from);
        return ResponseResult.GET(ResponseState.LOGIN_SUCCESS);
    }

    private String createTokenAndRefreshToken(NatureUser userFromDb, String from) {
        String oldTokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        // 将原来的refreshToken的tokenKey或者mobileTokenKey置空
        // 保证一个端不能有两个账号，redis要把前一个端的删除，不然redis中的还得包它2个小时
        RefreshToken refreshTokenFromDb= refreshTokenMapper.findOneByUserId(userFromDb.getId());
        if (Constants.User.FROM_PC.equals(from)){
            if (refreshTokenFromDb != null){
                redisUtil.del(Constants.User.KEY_TOKEN + refreshTokenFromDb.getTokenKey());
            }
            refreshTokenMapper.deleteTokenKeyByTokenKey(oldTokenKey);
        }else if (Constants.User.FROM_MOBILE.equals(from)){
            if (refreshTokenFromDb != null){
                redisUtil.del(Constants.User.KEY_TOKEN + refreshTokenFromDb.getMobileTokenKey());
            }
            refreshTokenMapper.deleteMobileTokenKeyByTokenKey(oldTokenKey);
        }
        // 生成token,包含from
        Map<String, Object> claims = ClaimsUtils.natureToClaims(userFromDb, from);
        String token = JwtUtil.createToken(claims);
        // 生成token Md5值 包含from字段
        String md5TokenKey = DigestUtils.md5DigestAsHex(token.getBytes());
        String tokenKey = from + md5TokenKey;
        // 将token放入redis
        redisUtil.set(Constants.User.KEY_TOKEN + tokenKey, token, Constants.TimeValueInSecond.HOUR * 2);
        // 将token Md5 放入cookie种返回到用户
        CookieUtils.setUpCookie(getResponse(), Constants.User.COOKIE_TOKEN_KEY, tokenKey);
        // 创建refreshTokenValue, 放入数据库
        String refreshTokenValue = JwtUtil.createRefreshToken(userFromDb.getId(), Constants.TimeValueInMillions.MONTH);
        // 将refreshToken补充数据，保存到数据库
        // 保存refreshToken 如果没有就保存，如果有就更新
        RefreshToken  refreshToken = refreshTokenFromDb;
        if (refreshToken == null){
            refreshToken = new RefreshToken();
            refreshToken.setId(String.valueOf(idWorker.nextId()));
            refreshToken.setUserId(userFromDb.getId());
            getCommonRefreshToken(refreshToken, refreshTokenValue, from, tokenKey);
            refreshTokenMapper.saveOne(refreshToken);
        }else {
            getCommonRefreshToken(refreshToken, refreshTokenValue, from, tokenKey);
            refreshTokenMapper.updatePartOne(refreshToken);
        }
        return tokenKey;
    }
    private void getCommonRefreshToken(RefreshToken refreshToken, String refreshTokenValue, String from, String tokenKey){
        refreshToken.setUpdateTime(new Date());
        refreshToken.setRefreshToken(refreshTokenValue);
        if (Constants.User.FROM_MOBILE.equals(from)){
            refreshToken.setMobileTokenKey(tokenKey);
        }else if (Constants.User.FROM_PC.equals(from)){
            refreshToken.setTokenKey(tokenKey);
        }
        refreshToken.setCreateTime(new Date());
    }
    private HttpServletRequest getRequest(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return  requestAttributes.getRequest();
    }

    private HttpServletResponse getResponse(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }
    // 1.从cookie拿到tokenKey
    // 2.parseTokenKey，从redis中拿到token，解析token，过期与redis中没有都为null
    // 3.token不行了，就安排refreshToken，如果为空，或者过期都null回去
    // 4.如果refreshToken可以就创建新token和refreshToken
    // 5.通过refreshToken userId查询到user
    // 6.删除原来的refreshToken
    // 7.通过user创建新token和refreshToken
    // 8.返回解析的user。
    @Override
    public NatureUser checkNatureUser() {
        // 1.拿到tokenKey
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        if (tokenKey == null){
            return null;
        }
        NatureUser natureUser = parseTokenKey(tokenKey);
        //TODO 逻辑问题
        String from  = tokenKey.substring(0,2);
        log.info("checkUser ==> " + from);
        if (natureUser == null){
            log.info("natureUser ==> null ==> To refreshToken");
            // 安排refreshToken
            RefreshToken refreshToken = null;
            if (Constants.User.FROM_PC.equals(from)){
                refreshToken = refreshTokenMapper.findOneByTokenKey(tokenKey);
            }else if (Constants.User.FROM_MOBILE.equals(from)){
                refreshToken = refreshTokenMapper.findOneByMobileTokenKey(tokenKey);
            }
            if (refreshToken == null){
                log.info("refreshToken ==> null");
                return null;
            }
            try{
                JwtUtil.parseJWT(refreshToken.getRefreshToken());
                // refreshToken OK
                // 查询user对象
                NatureUser userFromDb = userMapper.findOneById(refreshToken.getUserId());
                // 删除原来的refreshToken创建新token和refreshToken
                String newTokenKey = createTokenAndRefreshToken(userFromDb, from);
                return parseTokenKey(newTokenKey);
            }catch (Exception e){
                // refreshToken不行
                return null;
            }
        }
        return natureUser;
    }

    @Override
    public ResponseResult queryUserInfo(String userId) {
        NatureUser userFromDb = userMapper.findPartOneById(userId);
        if (userFromDb == null){
            return ResponseResult.FAILURE("用户信息不存在。");
        }
        return ResponseResult.SUCCESS("获取成功。").setData(userFromDb);
    }

    /**
     * 检查邮箱是否被注册
     * @param email
     * @return
     */
    @Override
    public ResponseResult checkEmail(String email) {
        NatureUser userFromByEmail = userMapper.findOneByEmail(email);
        return userFromByEmail != null ? ResponseResult.FAILURE("邮箱已经被注册。") : ResponseResult.SUCCESS("邮箱未被注册。");
    }

    /**
     * 检查用户名是否被注册
     * @param userName
     * @return
     */
    @Override
    public ResponseResult checkUserName(String userName) {
        NatureUser userFromByUserName = userMapper.findOneByUserName(userName);
        return userFromByUserName != null ? ResponseResult.FAILURE("用户名已被注册。") : ResponseResult.SUCCESS("用户名未被注册。");
    }

    @Override
    public ResponseResult updateUserInfo(String userId, NatureUser natureUser) {
        // 检查用户权限
        NatureUser userTokenKey = checkNatureUser();
        if (userTokenKey == null){
            return ResponseResult.GET(ResponseState.ACCOUNT_NOT_LOGIN);
        }
        // 获取用户信息
        // 查看用户id与修改的用户id是否一致
        NatureUser userFromDb = userMapper.findOneById(userTokenKey.getId());
        if (!userFromDb.getId().equals(userId)){
            return ResponseResult.GET(ResponseState.PERMISSION_DENY);
        }
        // 修改信息 用户名 头像 签名
        String userName = natureUser.getUserName();
        if (!TextUtils.isEmpty(userName) && !userTokenKey.getUserName().equals(natureUser.getUserName())){
            NatureUser userByUserName = userMapper.findOneByUserName(userName);
            if (userByUserName != null){
                return ResponseResult.FAILURE("用户名已注册。");
            }
            userFromDb.setUserName(userName);
        }
        String avatar = natureUser.getAvatar();
        if (!TextUtils.isEmpty(avatar)){
            userFromDb.setAvatar(avatar);
        }
        userFromDb.setSign(natureUser.getSign());
        log.info("userFromDb ==> regIp ==> " + userFromDb.getRegIp());
        // 更新
        boolean updateResult = userMapper.updatePartUser(userFromDb);
        // 干掉redis里的token，让它下一次访问的时候生成新的token和refresh信息，
        // 如果不干掉redis里的token，那redis的token是原来的值，不是修改后新的值
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        return updateResult ? ResponseResult.SUCCESS("修改用户信息成功。") : ResponseResult.SUCCESS("修改用户信息失败。");
    }

    /**
     * 删除用户，就是将状态改为state=0
     * 仅有管理员可做
     * @param userId
     * @return
     */
    @Override
    public ResponseResult deleteUser(String userId) {
        // 可以删除用户了
        boolean deleteResult = userMapper.deleteStateById(userId);
        if (deleteResult){
            return ResponseResult.SUCCESS("删除用户成功。");
        }
        return ResponseResult.FAILURE("删除用户失败。");
    }

    /**
     * 查询用户列表
     *
     * @param page
     * @param size
     * @param userName
     * @param email
     * @return
     */
    @Override
    public ResponseResult listUsers(int page, int size, String userName, String email) {
        // page和size控制
        page = checkPage(page);
        size =checkSize(size);
        // 可以查询用户列表了
        PageHelper.startPage(page ,size);
        log.info("userName ==> "+userName);
        log.info("email ==> "+ email);
        List<NatureUser> users = userMapper.findAllNoPassword(userName, email);
        PageInfo<NatureUser> pageInfo = new PageInfo<>(users);
        return ResponseResult.SUCCESS("获取用户列表成功").setData(pageInfo);
    }

    /**
     * 更新密码，
     * 通过邮箱的唯一性，发送邮件后，根据邮件验证码修改密码
     * 找回密码和更新密码都可以使用（逻辑通用的）
     * @param verifyCode
     * @param natureUser
     * @return
     */
    @Override
    public ResponseResult updatePassword(String verifyCode, NatureUser natureUser) {
        // 1.检查数据
        if (TextUtils.isEmpty(verifyCode)){
            return ResponseResult.FAILURE("邮箱验证码不能为空");
        }
        String email = natureUser.getEmail();
        if (TextUtils.isEmpty(email)){
            return ResponseResult.FAILURE("邮箱不能为空");
        }
        String rightVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CONTENT + email);
        if (TextUtils.isEmpty(rightVerifyCode) || !verifyCode.equals(rightVerifyCode)){
            return ResponseResult.FAILURE("验证码错误");
        }
        String password = natureUser.getPassword();
        if (TextUtils.isEmpty(password)){
            return ResponseResult.FAILURE("密码不能为空");
        }
        // 验证结束后，删除邮箱验证码
        redisUtil.del(Constants.User.KEY_EMAIL_CONTENT + email);
        // 可以修改密码了，通过邮箱修改
        boolean updateResult = userMapper.updateUserPasswordByEmail(bCryptPasswordEncoder.encode(password), email);
        return updateResult ? ResponseResult.SUCCESS("修改密码成功") : ResponseResult.FAILURE("修改密码失败");
    }

    @Override
    public ResponseResult updateUserEmail(String email, String verifyCode) {
        // 1.检查用户是否登录
        NatureUser natureUser = checkNatureUser();
        if (natureUser == null){
            return ResponseResult.GET(ResponseState.ACCOUNT_NOT_LOGIN);
        }
        // 2.检查数据
        if (TextUtils.isEmpty(email)){
            return ResponseResult.FAILURE("新邮箱不能为空");
        }
        if (TextUtils.isEmpty(verifyCode)){
            return ResponseResult.FAILURE("验证码不能为空");
        }
        // 3.比对验证码
        String rightVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CONTENT + email);
        if (TextUtils.isEmpty(rightVerifyCode) || !verifyCode.equals(rightVerifyCode)){
            return ResponseResult.FAILURE("验证码错误");
        }
        // 4.比对验证码完成后，删除redis中
        redisUtil.del(Constants.User.KEY_EMAIL_CONTENT + email);
        // 5.比对正确，修改邮箱地址
        boolean updateResult = userMapper.updateEmailById(email, natureUser.getId());
        return updateResult ? ResponseResult.SUCCESS("更改邮箱成功") : ResponseResult.FAILURE("更改邮箱失败");
    }

    @Override
    public ResponseResult doLogout() {
        // 1.检查是否登录
        NatureUser natureUser = checkNatureUser();
        if (natureUser == null){
            return ResponseResult.GET(ResponseState.ACCOUNT_NOT_LOGIN);
        }
        // 2.删除redis中的token
        // 获取tokenKey
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        // 3.删除refreshToken
        // 通过from置空tokenKey或者refreshTokenKey
        String from = parseFrom(tokenKey);
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        log.info("tokenKey ==> " + tokenKey);
        log.info("from ==> logout " + from);
        if (Constants.User.FROM_MOBILE.equals(from)){
            refreshTokenMapper.deleteMobileTokenKeyByTokenKey(tokenKey);
        }else if (Constants.User.FROM_PC.equals(from)){
            refreshTokenMapper.deleteTokenKeyByTokenKey(tokenKey);
        }
        // 4.删除cookie
        CookieUtils.deleteCookie(getResponse(),Constants.User.COOKIE_TOKEN_KEY);
        return ResponseResult.SUCCESS("退出登录成功");
    }

    @Override
    public ResponseResult parseToken() {
        NatureUser natureUser = checkNatureUser();
        if (natureUser == null){
            return ResponseResult.FAILURE("用户未登录");
        }
        return ResponseResult.SUCCESS("获取用户信息成功").setData(natureUser);
    }

    /**
     * 通过状态恢复用户
     * @param userId
     * @return
     */
    @Override
    public ResponseResult refreshUser(String userId) {
        return userMapper.refreshUserByState(userId) ? ResponseResult.SUCCESS("恢复用户成功") : ResponseResult.FAILURE("恢复用户失败");
    }

    @Override
    public ResponseResult resetPassword(String userId, String password) {
        if (TextUtils.isEmpty(userId)){
            return ResponseResult.FAILURE("用户ID不能为空");
        }
        if (TextUtils.isEmpty(password)){
            return ResponseResult.FAILURE("用户密码不能为空");
        }
        return userMapper.resetPassword(userId, bCryptPasswordEncoder.encode(password)) ? ResponseResult.SUCCESS("重置密码成功") : ResponseResult.FAILURE("重置密码失败");
    }

    @Override
    public ResponseResult getUserCount() {
        int userCount = userMapper.getUserCount();
        return ResponseResult.SUCCESS("获取用户总数成功").setData(userCount);
    }

    private String parseFrom(String tokenKey){
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        log.info("parse token ==> " + token);
        if (token != null){
            try{
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtils.getFrom(claims);
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }
    /**
     * 解析tokenKey
     * @param tokenKey token_key
     * @return
     */
    private NatureUser parseTokenKey(String tokenKey){
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        log.info("parse token ==> " + token);
        if (token != null){
            try{
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtils.claimsToNature(claims);
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }
}
