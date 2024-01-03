package net.nature.blog.mapper;

import net.nature.blog.pojo.FriendLink;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendLinkMapper {

    boolean saveOne(FriendLink friend);

    FriendLink findOneById(String id);

    List<FriendLink> findAll();

    List<FriendLink> findNormal();

    boolean deleteOneById(String id);

    boolean updatePartOne(FriendLink friendLinkFromDb);

    List<FriendLink> findAllByNODelete();
}
