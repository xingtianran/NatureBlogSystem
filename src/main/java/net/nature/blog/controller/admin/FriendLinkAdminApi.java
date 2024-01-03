package net.nature.blog.controller.admin;

import net.nature.blog.interceptor.CheckTooFrequentCommit;
import net.nature.blog.pojo.FriendLink;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IFriendLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/friend_link")
public class FriendLinkAdminApi {

    @Autowired
    private IFriendLinkService friendLinkService;

    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult addFriendLink(@RequestBody FriendLink friendLink){
        return friendLinkService.addFriendLink(friendLink);
    }

    @CheckTooFrequentCommit
    @PutMapping("/{friend_link_id}")
    public ResponseResult updateFriendLink(@PathVariable("friend_link_id") String friendLinkId, @RequestBody FriendLink friendLink){
        return friendLinkService.updateFriendLink(friendLinkId, friendLink);
    }

    @GetMapping("/{friend_link_id}")
    public  ResponseResult getFriendLink(@PathVariable("friend_link_id") String friendLinkId){
        return friendLinkService.getFriendLink(friendLinkId);
    }

    @DeleteMapping("/{friend_link_id}")
    public ResponseResult deleteFriendLink(@PathVariable("friend_link_id")String friendLinkId){
        return friendLinkService.deleteFriendLink(friendLinkId);
    }

    @GetMapping("/list/{page}/{size}")
    public ResponseResult listFriendLinks(@PathVariable("page")int page, @PathVariable("size")int size){
        return friendLinkService.listFriendLinks(page, size);
    }
}
