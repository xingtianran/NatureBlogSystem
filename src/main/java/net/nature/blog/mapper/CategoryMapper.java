package net.nature.blog.mapper;

import net.nature.blog.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {


    boolean saveOne(Category category);

    Category findOneById(String id);

    List<Category> findAll();

    boolean updatePartOne(Category categoryFromDb);

    boolean deleteCategoryByStatus(String id);

    List<Category> findAllNoDelete();

    boolean updateCategoryState(String id);

    String getCategoryNameById(String id);
}
