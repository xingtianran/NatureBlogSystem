package net.nature.blog.services;

import net.nature.blog.pojo.Article;
import net.nature.blog.response.ResponseResult;

public interface IElasticsearchService {
    ResponseResult doSearch(String keyword, String categoryId, String label, String sort, int page, int size);

    ResponseResult doSearch();
    void addArticle(Article article);

    void deleteArticle(String articleId);

    void updateArticle(Article article);
}
