package net.nature.blog.services;

import net.nature.blog.pojo.Category;
import net.nature.blog.response.ResponseResult;

public interface ICategoryService {
    ResponseResult addCategory(Category category);

    ResponseResult getCategory(String categoryId);

    ResponseResult listCategories(int page, int size);

    ResponseResult updateCategory(String categoryId, Category category);

    ResponseResult deleteCategory(String categoryId);

    ResponseResult refreshCategory(String categoryId);

    ResponseResult getAllCategories();

    ResponseResult getCategoryName(String categoryId);

    ResponseResult listCategories();
}
