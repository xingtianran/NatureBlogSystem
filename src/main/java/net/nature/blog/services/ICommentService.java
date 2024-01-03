package net.nature.blog.services;

import net.nature.blog.pojo.Comment;
import net.nature.blog.response.ResponseResult;

import java.util.List;

public interface ICommentService {
    ResponseResult addComment(Comment comment);

    ResponseResult listComments(String articleId, int page, int size);
    ResponseResult listComments(int page, int size, String articleId);

    ResponseResult deleteComment(String commentId);

    ResponseResult resetTopComment(String commentId);

    ResponseResult getCommentsNew(int size);

    ResponseResult getCommentCount();
    ResponseResult getCommentCount(String articleId);

    ResponseResult getCommentsByArticleId(String articleId);

    List<Comment> handleComment(List<Comment> parentComment, List<Comment> childComment);
}
