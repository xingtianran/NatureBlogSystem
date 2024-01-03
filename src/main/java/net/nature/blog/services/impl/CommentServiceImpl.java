package net.nature.blog.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.mapper.ArticleMapper;
import net.nature.blog.mapper.CommentMapper;
import net.nature.blog.pojo.Article;
import net.nature.blog.pojo.Comment;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.response.ResponseState;
import net.nature.blog.services.ICommentService;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.RedisUtil;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
public class CommentServiceImpl extends BaseService implements ICommentService {

    @Autowired
    private IUserService userService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Gson gson;

    @Override
    public ResponseResult addComment(Comment comment) {
        // 1.检查是否登录
        NatureUser natureUser = userService.checkNatureUser();
        if (natureUser == null){
            return ResponseResult.GET(ResponseState.ACCOUNT_NOT_LOGIN);
        }
        // 2.检查数据
        String articleId = comment.getArticleId();
        if (TextUtils.isEmpty(articleId)){
            return ResponseResult.FAILURE("文章id不能为空");
        }
        Article article = articleMapper.findOneById(articleId);
        if (article == null){
            return ResponseResult.FAILURE("文章不存在");
        }
        String content = comment.getContent();
        if (TextUtils.isEmpty(content)){
            return ResponseResult.FAILURE("文章内容不能为空");
        }
        if (TextUtils.isEmpty(comment.getParentId())){
            comment.setParentId(null);
        }
        // 3.补全数据
        comment.setId(String.valueOf(idWorker.nextId()));
        comment.setUserId(natureUser.getId());
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        // 4.保存到数据
        boolean result = commentMapper.saveOne(comment);
        // 5.删除redis中的缓存评论
        redisUtil.del(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + comment.getArticleId());
        //TODO 可以邮件通知对方
        return result ? ResponseResult.SUCCESS("评论成功") : ResponseResult.FAILURE("评论失败");
    }

    /**
     * 获取文章全部评论
     * 先发表的会在一定时间内排在前边，过了这个时间，按照点赞量和时间排序
     * @param articleId 文章id
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listComments(String articleId, int page, int size) {
        page =checkPage(page);
        size = checkSize(size);
        // 缓存文章评论第一页的内容
        if (page == 1){
            String redisComment = (String) redisUtil.get(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId);
            if (!TextUtils.isEmpty(redisComment)){
                PageInfo<Comment> commentList = gson.fromJson(redisComment,new TypeToken<PageInfo<Comment>>() {
                }.getType());
                log.info("comment from redis...");
                return ResponseResult.SUCCESS("获取评论成功").setData(commentList);
            }
        }
        // 1.检查文章是否存在
        Article article = articleMapper.findOneById(articleId);
        if (article == null){
            return ResponseResult.FAILURE("文章不存在");
        }
        // 2.通过文章id查询评论
        PageHelper.startPage(page, size);
         List<Comment> comments = commentMapper.findAllYesParentByArticleId(articleId);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        // 将评论放到redis
        if (page == 1){
            redisUtil.set(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId, gson.toJson(pageInfo), Constants.TimeValueInSecond.HOUR);
        }
        return ResponseResult.SUCCESS("获取评论成功").setData(pageInfo);
    }

    @Override
    public ResponseResult listComments(int page, int size, String articleId) {
        page = checkPage(page);
        size = checkSize(size);
        // 置顶最先查出来
        PageHelper.startPage(page, size);
        // 按置顶状态与创建时间排序
        List<Comment> comments = commentMapper.findAll(articleId);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        return ResponseResult.SUCCESS("获取评论列表成功").setData(pageInfo);
    }

    @Override
    public ResponseResult deleteComment(String commentId) {
        // 1.检查用户是否登录
        NatureUser natureUser = userService.checkNatureUser();
        if (natureUser == null){
            return ResponseResult.GET(ResponseState.ACCOUNT_NOT_LOGIN);
        }
        // 2.判断是不是这个用户的评论
        String commentUserId = commentMapper.findOneUserIdById(commentId);
        if (commentUserId == null){
            return ResponseResult.FAILURE("评论不存在");
        }
        if (!natureUser.getId().equals(commentUserId)
                && !Constants.User.ROLE_ADMIN.equals(natureUser.getRoles())){
            return ResponseResult.GET(ResponseState.PERMISSION_DENY);
        }
        return commentMapper.deleteOneById(commentId) ? ResponseResult.SUCCESS("删除评论成功") : ResponseResult.FAILURE("删除评论失败");
    }

    /**
     * 置顶评论
     * @param commentId
     * @return
     */
    @Override
    public ResponseResult resetTopComment(String commentId) {
        String topFromDb = commentMapper.findOneTopById(commentId);
        if (topFromDb == null){
            return ResponseResult.FAILURE("评论不存在");
        }
        boolean result = false;
        if (Constants.Comment.NO_TOP.equals(topFromDb)){
            result = commentMapper.updatePartOne(commentId, Constants.Comment.YES_TOP);
        }else if (Constants.Comment.YES_TOP.equals(topFromDb)){
            result = commentMapper.updatePartOne(commentId, Constants.Comment.NO_TOP);
        }else {
            return ResponseResult.FAILURE("无法进行操作");
        }
        return result ? ResponseResult.SUCCESS(Constants.Comment.NO_TOP.equals(topFromDb) ? "置顶成功" : "取消置顶成功") :
                ResponseResult.FAILURE(Constants.Comment.NO_TOP.equals(topFromDb) ? "置顶失败" : "取消置顶失败");
    }

    @Override
    public ResponseResult getCommentsNew(int size) {
        List<Comment> commentsNew = commentMapper.findCommentsNew(size);
        return ResponseResult.SUCCESS("获取最新评论成功").setData(commentsNew);
    }

    @Override
    public ResponseResult getCommentCount() {
        return ResponseResult.SUCCESS("获取评论总数成功").setData(commentMapper.getCommentCount());
    }

    @Override
    public ResponseResult getCommentCount(String articleId) {
        return ResponseResult.SUCCESS("获取评论总数成功").setData(commentMapper.getCommentCountByArticle(articleId));
    }

    /**
     * 通过文章ID获取全部评论
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult getCommentsByArticleId(String articleId) {
        // 根评论
        List<Comment> parentComments = commentMapper.findAllNoParentByArticleId(articleId);
        // 子评论
        List<Comment> childComments = commentMapper.findAllYesParentByArticleId(articleId);
        // 将子评论添加到List
        handleComment(parentComments, childComments);
        return ResponseResult.SUCCESS("获取该文章评论成功").setData(parentComments);
    }

    public List<Comment> handleComment(List<Comment> parentComment, List<Comment> childComment){
        while (!childComment.isEmpty()){
            // 取出子评论，放如父评论Node下
            int size = childComment.size();
            for (int i = 0; i < size; i++){
                if (addComment(parentComment, childComment.get(i))) {
                    childComment.remove(i);
                    i--;
                    size--;
                }
            }
        }
        return parentComment;
    }

    public boolean addComment(List<Comment> parentComment, Comment childComment){
        // 只能允许有一个后继节点
        for (Comment parentFromList : parentComment) {
            if (parentFromList.getId().equals(childComment.getParentId())){
                // 父对象的子List中
                parentFromList.getChildComment().add(childComment);
                return true;
            }else {
                // 遍历父节点下的子集合
                for (Comment commentChildList : parentFromList.getChildComment()){
                    if (commentChildList.getId().equals(childComment.getParentId())){
                        parentFromList.getChildComment().add(childComment);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
