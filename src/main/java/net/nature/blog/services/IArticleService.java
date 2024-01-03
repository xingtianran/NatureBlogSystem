package net.nature.blog.services;

import net.nature.blog.pojo.Article;
import net.nature.blog.response.ResponseResult;

public interface IArticleService {
    ResponseResult addArticle(Article article);

    ResponseResult getArticle(String articleId, String isCache);

    ResponseResult listArticles(int page, int size, String keyword, String categoryId, String state, String label, String isCache, String top);

    ResponseResult updateArticle(String articleId, Article article);

    ResponseResult deleteArticle(String articleId);

    ResponseResult deleteArticleByState(String articleId);

    ResponseResult resetTopArticle(String articleId);

    ResponseResult getTopArticle();

    ResponseResult getRecommendArticle(String articleId, int size);

    ResponseResult getLabels(int size);

    ResponseResult getArticleCount();
}
