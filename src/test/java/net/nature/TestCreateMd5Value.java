package net.nature;

import org.springframework.util.DigestUtils;

public class TestCreateMd5Value {
    public static void main(String[] args) {

        // ed755f7c166df7f72bb004f6e531aedb
        String md5 = DigestUtils.md5DigestAsHex("nature_blog_system_-+".getBytes());
        System.out.println(md5);
    }
}
