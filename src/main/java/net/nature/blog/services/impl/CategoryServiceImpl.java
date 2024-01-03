package net.nature.blog.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import net.nature.blog.mapper.CategoryMapper;
import net.nature.blog.pojo.Category;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.ICategoryService;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl extends BaseService implements ICategoryService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private IUserService userService;

    @Override
    public ResponseResult addCategory(Category category) {
        // 1.检查数据
        if (TextUtils.isEmpty(category.getName())) {
            return ResponseResult.FAILURE("分类名称不能为空");
        }
        if (TextUtils.isEmpty(category.getPinyin())){
            return ResponseResult.FAILURE("分类拼音不能为空");
        }
        if(TextUtils.isEmpty(category.getDescription())){
            return ResponseResult.FAILURE("分类描述不能为空");
        }
        // 2.补全数据
        category.setId(String.valueOf(idWorker.nextId()));
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        // 3.保存数据
        return categoryMapper.saveOne(category) ? ResponseResult.SUCCESS("添加分类成功") : ResponseResult.FAILURE("添加分类失败");
    }

    @Override
    public ResponseResult getCategory(String categoryId) {
        Category categoryFromDb = categoryMapper.findOneById(categoryId);
        if (categoryFromDb == null){
            return ResponseResult.FAILURE("分类信息不存在");
        }
        return ResponseResult.SUCCESS("分类信息获取成功").setData(categoryFromDb);
    }
    /**
     * 管理员可以获取全部状态分类，用户只能获取状态为1的和没有登录的
     * 但是如果状态为删除的分类，就不能拿到了
     * @return
     */
    @Override
    public ResponseResult listCategories(int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        NatureUser natureUser = userService.checkNatureUser();
        PageInfo<Category> pageInfo = null;
        if (natureUser == null || Constants.User.ROLE_NORMAL.equals(natureUser.getRoles())){
            PageHelper.startPage(page, size);
            List<Category> categories = categoryMapper.findAllNoDelete();
            pageInfo = new PageInfo<>(categories);
        } else{
            PageHelper.startPage(page, size);
            List<Category> categories = categoryMapper.findAll();
            pageInfo = new PageInfo<>(categories);
        }
        return ResponseResult.SUCCESS("获取分类列表成功").setData(pageInfo);
    }


    @Override
    public ResponseResult updateCategory(String categoryId, Category category) {
        Category categoryFromDb = categoryMapper.findOneById(categoryId);
        if (categoryFromDb == null){
            return ResponseResult.FAILURE("分类不存在");
        }
        String name = category.getName();
        if (!TextUtils.isEmpty(name)){
            categoryFromDb.setName(name);
        }
        String pinyin = category.getPinyin();
        if (!TextUtils.isEmpty(pinyin)){
            categoryFromDb.setPinyin(pinyin);
        }
        String description = category.getDescription();
        if (!TextUtils.isEmpty(description)){
            categoryFromDb.setDescription(description);
        }
        String state = category.getState();
        if (Constants.DEFAULT_STATE.equals(state) || Constants.DISABLE_STATE.equals(state)){
            categoryFromDb.setState(state);
        }
        categoryFromDb.setOrder(category.getOrder());
        categoryFromDb.setUpdateTime(new Date());
        boolean updateResult = categoryMapper.updatePartOne(categoryFromDb);
        return updateResult ? ResponseResult.SUCCESS("分类更新成功") : ResponseResult.FAILURE("分类更新失败");
    }

    /**
     * 修改状态为0，不是真删除
     * @param categoryId
     * @return
     */
    @Override
    public ResponseResult deleteCategory(String categoryId) {
        return categoryMapper.deleteCategoryByStatus(categoryId) ? ResponseResult.SUCCESS("禁用分类成功") : ResponseResult.FAILURE("禁用分类失败");
    }

    /**
     * 恢复分类
     * @param categoryId
     * @return
     */
    @Override
    public ResponseResult refreshCategory(String categoryId) {
        return categoryMapper.updateCategoryState(categoryId) ? ResponseResult.SUCCESS("恢复分类成功") : ResponseResult.FAILURE("恢复分类失败");
    }

    @Override
    public ResponseResult getAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        return ResponseResult.SUCCESS("获取全部分类").setData(categories);
    }

    @Override
    public ResponseResult getCategoryName(String categoryId) {
        String categoryName = categoryMapper.getCategoryNameById(categoryId);
        return categoryName != null ? ResponseResult.SUCCESS("获取分类名称成功").setData(categoryName) : ResponseResult.FAILURE("获取分类名称失败");
    }

    @Override
    public ResponseResult listCategories() {
        List<Category> categories = categoryMapper.findAll();
        return ResponseResult.SUCCESS("获取全部标签成功").setData(categories);
    }
}
