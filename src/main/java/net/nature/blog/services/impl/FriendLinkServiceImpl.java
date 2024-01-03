package net.nature.blog.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import net.nature.blog.mapper.FriendLinkMapper;
import net.nature.blog.pojo.FriendLink;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IFriendLinkService;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class FriendLinkServiceImpl extends BaseService implements IFriendLinkService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private FriendLinkMapper friendLinkMapper;

    @Autowired
    private IUserService userService;

    @Override
    public ResponseResult addFriendLink(FriendLink friend) {
        // 1.检查数据
        if (TextUtils.isEmpty(friend.getName())){
            return ResponseResult.FAILURE("友链名字不能为空");
        }
        if (TextUtils.isEmpty(friend.getLogo())){
            return ResponseResult.FAILURE("友链logo不能为空");
        }
        if (TextUtils.isEmpty(friend.getUrl())){
            return ResponseResult.FAILURE("友链url不能为空");
        }
        // 2.补充数据
        friend.setId(String.valueOf(idWorker.nextId()));
        friend.setCreateTime(new Date());
        friend.setUpdateTime(new Date());
        // 3.保存数据
        return friendLinkMapper.saveOne(friend) ? ResponseResult.SUCCESS("添加友情链接成功") : ResponseResult.FAILURE("添加友情链接失败");
    }

    @Override
    public ResponseResult getFriendLink(String friendLinkId) {
        FriendLink friendLink = friendLinkMapper.findOneById(friendLinkId);
        if (friendLink == null){
            return ResponseResult.FAILURE("友情链接不存在");
        }
        return ResponseResult.SUCCESS("获取友情链接成功").setData(friendLink);
    }

    @Override
    public ResponseResult listFriendLinks(int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        NatureUser natureUser = userService.checkNatureUser();
        PageInfo<FriendLink> pageInfo = null;
        if (natureUser == null || !Constants.User.ROLE_ADMIN.equals(natureUser.getRoles())){
            PageHelper.startPage(page, size);
            List<FriendLink> friendLinks = friendLinkMapper.findAllByNODelete();
            pageInfo = new PageInfo<>(friendLinks);
        }else{
            PageHelper.startPage(page, size);
            List<FriendLink> friendLinks = friendLinkMapper.findAll();
            pageInfo = new PageInfo<>(friendLinks);
        }
        return ResponseResult.SUCCESS("获取友链列表成功").setData(pageInfo);
    }

    @Override
    public ResponseResult listFriendLinks() {
        List<FriendLink> friendLinks = friendLinkMapper.findNormal();
        return ResponseResult.SUCCESS("获取友情链接成功").setData(friendLinks);
    }

    /**
     * 真删除，不改状态
     * @param friendLinkId
     * @return
     */
    @Override
    public ResponseResult deleteFriendLink(String friendLinkId) {
        return friendLinkMapper.deleteOneById(friendLinkId) ? ResponseResult.SUCCESS("友链删除成功") : ResponseResult.FAILURE("友链删除失败");
    }

    @Override
    public ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink) {
        FriendLink friendLinkFromDb = friendLinkMapper.findOneById(friendLinkId);
        if (friendLinkFromDb == null){
            return ResponseResult.FAILURE("友链不存在");
        }
        // 补全数据
        friendLinkFromDb.setName(friendLink.getName());
        friendLinkFromDb.setLogo(friendLink.getLogo());
        friendLinkFromDb.setUrl(friendLink.getUrl());
        friendLinkFromDb.setOrder(friendLink.getOrder());
        String state = friendLink.getState();
        if (Constants.DISABLE_STATE.equals(state) || Constants.DEFAULT_STATE.equals(state)){
            friendLinkFromDb.setState(state);
        }
        // 保存到数据库
        boolean updateResult = friendLinkMapper.updatePartOne(friendLinkFromDb);
        return updateResult ? ResponseResult.SUCCESS("更新友链成功") : ResponseResult.FAILURE("更新友链失败");
    }
}
