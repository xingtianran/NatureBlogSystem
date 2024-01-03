package net.nature.blog.mapper;

import net.nature.blog.pojo.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {

    boolean saveOne(Article article);

    Article findOneById(String id);
    boolean updateOneById(Article article);

    List<Article> findAll();
    List<Article> findPartAllByKeywordOrCategoryIdOrLabelNoContent(String keyword, String categoryId, String state, String label, String top);

    Article findOneByAndUserIdNoPassword(String id);

    boolean updatePartOneById(Article articleFromDb);

    boolean deleteOneById(String id);

    boolean deleteOneByUpdateState(String articleId);

    boolean updateOneToTop(String articleId);

    boolean updateOneNoTop(String articleId);

    List<Article> findAllByTop(String top);

    List<Article> findAllByLabel(String label, String id, int size);

    List<Article> findNewAll(int size, String articleId, List<Article> articleList);

    List<Article> findNewSpecialAll(@Param("size") int span, @Param("originalId") String originalId, @Param("existId") String existId);

    void updateViewCountById(long newViewCount, String id);

    int getArticleCount();
}
