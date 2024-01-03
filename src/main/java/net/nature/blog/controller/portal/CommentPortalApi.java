package net.nature.blog.controller.portal;

import net.nature.blog.interceptor.CheckTooFrequentCommit;
import net.nature.blog.pojo.Comment;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/comment")
public class CommentPortalApi {

    @Autowired
    private ICommentService commentService;

    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult addComment(@RequestBody Comment comment){
        return commentService.addComment(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId")String commentId){
        return commentService.deleteComment(commentId);
    }

    @GetMapping("/list/{articleId}/{page}/{size}")
    public ResponseResult listComments(@PathVariable("articleId")String articleId,
                                       @PathVariable("page")int page,
                                       @PathVariable("size")int size){
        return commentService.listComments(articleId, page, size);
    }

    @GetMapping("/article/{articleId}")
    public ResponseResult getCommentsByArticleId(@PathVariable("articleId")String articleId){
        return commentService.getCommentsByArticleId(articleId);
    }
    @GetMapping("/count/{articleId}")
    public ResponseResult getCommentCount(@PathVariable("articleId")String articleId){
        return commentService.getCommentCount(articleId);
    }
}
