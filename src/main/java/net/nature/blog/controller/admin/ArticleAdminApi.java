package net.nature.blog.controller.admin;

import net.nature.blog.interceptor.CheckTooFrequentCommit;
import net.nature.blog.pojo.Article;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IArticleService;
import net.nature.blog.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/article")
public class ArticleAdminApi {

    @Autowired
    private IArticleService articleService;

    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult addArticle(@RequestBody Article article){
        return articleService.addArticle(article);
    }

    @CheckTooFrequentCommit
    @PutMapping("/{articleId}")
    public ResponseResult updateArticle(@PathVariable("articleId") String articleId, @RequestBody Article article){
        return articleService.updateArticle(articleId, article);
    }

    @GetMapping("/{articleId}")
    public  ResponseResult getArticle(@PathVariable("articleId") String articleId,
                                      @RequestParam(value = "isAdmin", required = false)String isAdmin){
        return articleService.getArticle(articleId, isAdmin);
    }

    /**
     * 多用户的话，用户最好不能删除，只能管理员删除
     * @param articleId
     * @return
     */
    @DeleteMapping("/{articleId}")
    public ResponseResult deleteArticle(@PathVariable("articleId")String articleId){
        return articleService.deleteArticle(articleId);
    }

    @GetMapping("/list/{page}/{size}")
    public ResponseResult listArticles(@PathVariable("page")int page,
                                       @PathVariable("size")int size,
                                       @RequestParam(value = "keyword", required = false)String keyword,
                                       @RequestParam(value = "categoryId", required = false)String categoryId,
                                       @RequestParam(value = "state", required = false)String state){
        return articleService.listArticles(page, size, keyword, categoryId, state, null, Constants.No_CACHE, null);
    }

    /**
     * 通过更改状态删除文章
     * @param articleId
     * @return
     */
    @DeleteMapping("/state/{articleId}")
    public ResponseResult deleteArticleByState(@PathVariable("articleId")String articleId){
        return articleService.deleteArticleByState(articleId);
    }


    /**
     * 设置文章置顶，如果文章是置顶的就设置成取消置顶
     * @param articleId
     * @return
     */
    @PutMapping("/top/{articleId}")
    public ResponseResult resetTopArticle(@PathVariable("articleId")String articleId){
        return articleService.resetTopArticle(articleId);
    }

    @GetMapping("/count")
    public ResponseResult getArticleCount(){
        return articleService.getArticleCount();
    }
}
