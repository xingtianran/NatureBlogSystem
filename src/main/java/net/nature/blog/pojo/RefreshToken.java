package net.nature.blog.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class RefreshToken {

    private String id;

    private String refreshToken;

    private String userId;

    private String tokenKey;

    private String mobileTokenKey;

    private Date createTime;

    private Date updateTime;
}
