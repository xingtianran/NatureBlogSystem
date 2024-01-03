package net.nature.blog.services.impl;

import net.nature.blog.pojo.NatureUser;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.CookieUtils;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Service("permission")
public class PermissionService {

    @Autowired
    private IUserService userService;

    public boolean admin(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        // 先判断cookie
        String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
        if (TextUtils.isEmpty(tokenKey)){
            return false;
        }
        // 验证身份
        NatureUser currentUser = userService.checkNatureUser();
        if (currentUser == null){
            return false;
        }
        if (Constants.User.ROLE_ADMIN.equals(currentUser.getRoles())){
            return true;
        }
        return false;
    }
}
