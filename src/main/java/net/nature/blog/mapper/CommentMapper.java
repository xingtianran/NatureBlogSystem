package net.nature.blog.mapper;

import net.nature.blog.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    boolean saveOne(Comment comment);

    List<Comment> findAllYesParentByArticleId(String articleId);
    List<Comment> findAllNoParentByArticleId(String articleId);
    List<Comment> findCommentsNew(int size);

    boolean deleteOneById(String id);

    void deleteByArticleId(String articleId);
    String findOneUserIdById(String id);

    String findOneTopById(String id);

    boolean updatePartOne(String id, String top);

    List<Comment> findAll(String articleId);

    int getCommentCount();

    int getCommentCountByArticle(String articleId);
}
