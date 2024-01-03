package net.nature.blog.mapper;

import net.nature.blog.pojo.RefreshToken;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface RefreshTokenMapper {

    RefreshToken findOneByTokenKey(String tokenKey);

    boolean saveOne(RefreshToken refreshToken);

    int deleteAllByUserId(String userId);

    void deleteTokenKeyByTokenKey(String tokenKey);

    void deleteMobileTokenKeyByTokenKey(String mobileTokenKey);

    RefreshToken findOneByUserId(String id);

    RefreshToken findOneByMobileTokenKey(String mobileTokenKey);

    void updatePartOne(RefreshToken refreshToken);

}
