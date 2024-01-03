package net.nature;

import io.jsonwebtoken.Claims;

import net.nature.blog.utils.JwtUtil;

public class TestParseToken {
    public static void main(String[] args) {
        Claims claims = JwtUtil.parseJWT("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMTY2MDc1NjkzNjg4MTYwMjU2IiwiaWF0IjoxNjk4MjI4NzgyLCJleHAiOjE3MDA4MjA3ODJ9.zm1l6Vjd3McpPvhMrozRV6PNiK03fgcG0ZI2rkAvttI");
        String id = (String) claims.getId();
        String userName = (String) claims.get("userName");
        System.out.println("id ==> " + id);
        System.out.println("userName ==> " + userName);
    }
}
