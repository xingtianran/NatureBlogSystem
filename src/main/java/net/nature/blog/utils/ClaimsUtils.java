package net.nature.blog.utils;

import net.nature.blog.pojo.NatureUser;
import java.util.HashMap;
import java.util.Map;

public class ClaimsUtils {
    public static final String ID = "id";
    public static final String USER_NAME = "user_name";

    public static final String ROLES = "roles";

    public static final String AVATAR = "avatar";

    public static final String EMAIL = "email";

    public static final String SIGN = "sign";

    public static final String FROM = "from";
    public static Map<String, Object> natureToClaims(NatureUser natureUser, String from){
        Map<String, Object> claims = new HashMap<>();
        claims.put(ID, natureUser.getId());
        claims.put(USER_NAME, natureUser.getUserName());
        claims.put(ROLES, natureUser.getRoles());
        claims.put(AVATAR, natureUser.getAvatar());
        claims.put(EMAIL, natureUser.getEmail());
        claims.put(SIGN, natureUser.getSign());
        claims.put(FROM, from);
        return claims;
    }

    public static String getFrom(Map<String, Object> claims){
        return (String) claims.get(FROM);
    }
    public static NatureUser claimsToNature(Map<String, Object> claims){
        NatureUser natureUser = new NatureUser();
        String id = (String) claims.get(ID);
        natureUser.setId(id);
        String userName = (String) claims.get(USER_NAME);
        natureUser.setUserName(userName);
        String roles = (String) claims.get(ROLES);
        natureUser.setRoles(roles);
        String avatar = (String) claims.get(AVATAR);
        natureUser.setAvatar(avatar);
        String email = (String) claims.get(EMAIL);
        natureUser.setEmail(email);
        String sign = (String) claims.get(SIGN);
        natureUser.setSign(sign);
        return natureUser;
    }
}
