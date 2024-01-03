package net.nature.blog.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static final String domain = "xtran.top";

    public static final int defaultAge = Constants.TimeValueInSecond.YEAR;

    public static void setUpCookie(HttpServletResponse response, String key, String value){
        setUpCookie(response, key, value, defaultAge);
    }

    /**
     * 创建cookie
     * @param response
     * @param key cookie_key
     * @param value cookie_value
     * @param age cookie有效期
     */
    public static void setUpCookie(HttpServletResponse response, String key, String value, int age){
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setDomain(domain);
        cookie.setMaxAge(age);
        response.addCookie(cookie);
    }

    /**
     * 获取cookie
     * @param request
     * @param key cookie_key
     * @return
     */
    public static String getCookie(HttpServletRequest request, String key){
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie e : cookies){
            if (key.equals(e.getName())){
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * 删除cookie
     * @param response
     * @param key cookie_key
     */
    public static void deleteCookie(HttpServletResponse response, String key){
        setUpCookie(response, key, null, 0);
    }


}
