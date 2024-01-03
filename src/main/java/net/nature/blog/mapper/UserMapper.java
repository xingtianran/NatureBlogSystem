package net.nature.blog.mapper;

import net.nature.blog.pojo.NatureUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {

    boolean saveOne(NatureUser natureUser);

    NatureUser findOneByUserName(String username);

    NatureUser findOneByEmail(String email);

    NatureUser findOneById(String id);

    boolean updateUserPasswordByEmail(String password, String email);

    boolean deleteStateById(String id);

    List<NatureUser> findAllNoPassword(String userName, String email);

    boolean updateEmailById(String email, String id);

    boolean updatePartUser(NatureUser natureUser);

    NatureUser findPartOneById(String id);

    void updateTimeAndIp(String id, Date updateTime, String loginIp);

    boolean refreshUserByState(String id);

    boolean resetPassword(String userId, String password);

    int getUserCount();
}
