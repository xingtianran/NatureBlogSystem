package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NatureUser {

    private String id;


    private String userName;


    private String password;


    private String roles;


    private String avatar;


    private String email;


    private String sign;


    private String state;


    private String regIp;

    private String loginIp;

    private Date createTime;

    private Date updateTime;
}
