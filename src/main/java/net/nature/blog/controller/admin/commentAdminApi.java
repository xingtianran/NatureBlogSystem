package net.nature.blog.controller.admin;

import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/comment")
public class commentAdminApi {

    @Autowired
    private ICommentService commentService;

    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId")String commentId){
        return commentService.deleteComment(commentId);
    }

    @GetMapping("/list/{page}/{size}")
    public ResponseResult listComments(@PathVariable("page")int page,
                                       @PathVariable("size")int size,
                                       @RequestParam(value = "articleId", required = false)String articleId){
        return commentService.listComments(page, size, articleId);
    }

    @PutMapping("/top/{commentId}")
    public ResponseResult resetTopComment(@PathVariable("commentId")String commentId){
        return commentService.resetTopComment(commentId);
    }

    @GetMapping("/new/{size}")
    public ResponseResult getCommentsNew(@PathVariable("size")int size){
        return commentService.getCommentsNew(size);
    }

    @GetMapping("/count")
    public ResponseResult getCommentCount(){
        return commentService.getCommentCount();
    }
}
