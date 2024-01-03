package net.nature.blog.services;

import net.nature.blog.pojo.FriendLink;
import net.nature.blog.response.ResponseResult;

public interface IFriendLinkService {
    ResponseResult addFriendLink(FriendLink friend);

    ResponseResult getFriendLink(String friendId);

    ResponseResult listFriendLinks(int page, int size);

    ResponseResult listFriendLinks();

    ResponseResult deleteFriendLink(String friendLinkId);

    ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink);
}
