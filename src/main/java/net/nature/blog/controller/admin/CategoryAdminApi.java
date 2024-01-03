package net.nature.blog.controller.admin;

import net.nature.blog.interceptor.CheckTooFrequentCommit;
import net.nature.blog.pojo.Category;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 * admin包权限只能是管理员
 */
@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/category")
public class CategoryAdminApi {

    @Autowired
    private ICategoryService categoryService;
    /**
     * 添加分类
     * @param category
     * @return
     */
    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult addCategory(@RequestBody Category category){
        return categoryService.addCategory(category);
    }

    /**
     * 删除分类（修改状态为0，不是真删除）
     * @param categoryId
     * @return
     */
    @DeleteMapping("/{categoryId}")
    public ResponseResult deleteCategory(@PathVariable("categoryId") String categoryId){
        return categoryService.deleteCategory(categoryId);
    }

    /**
     * 修改分类信息
     * @param categoryId
     * @return
     */
    @CheckTooFrequentCommit
    @PutMapping("/{categoryId}")
    public ResponseResult updateCategory(@PathVariable("categoryId") String categoryId, @RequestBody Category category){
        return categoryService.updateCategory(categoryId, category);
    }

    /**
     * 获取分类信息
     * @param categoryId
     * @return
     */
    @GetMapping("/{category_id}")
    public ResponseResult getCategory(@PathVariable("category_id") String categoryId){
        return categoryService.getCategory(categoryId);
    }

    /**
     * 获取全部分类
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listCategories(@PathVariable("page")int page, @PathVariable("size")int size){
        return categoryService.listCategories(page, size);
    }

    @PutMapping("/state/{categoryId}")
    public ResponseResult refreshCategory(@PathVariable("categoryId")String categoryId){
        return categoryService.refreshCategory(categoryId);
    }

    @GetMapping("/all")
    public ResponseResult getAllCategories(){
        return categoryService.getAllCategories();
    }

    @GetMapping("/name/{category_id}")
    public ResponseResult getCategoryName(@PathVariable("category_id")String categoryId){
        return categoryService.getCategoryName(categoryId);
    }
}
