package net.nature.blog.controller.portal;

import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IArticleService;
import net.nature.blog.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/article")
public class ArticlePortalApi {

    @Autowired
    private IArticleService articleService;

    /**
     * 获取全部文章，用户和非登录用户只能获取publish状态的
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listArticles(@PathVariable("page")int page,
                                       @PathVariable("size")int size,
                                       @RequestParam(value = "categoryId", required = false)String categoryId){
        return articleService.listArticles(page, size, null, categoryId, Constants.Article.STATE_PUBLISH,null, Constants.YES_CACHE, Constants.Article.NO_TOP);
    }


    @GetMapping("/top")
    public ResponseResult getTopArticle(){
        return articleService.getTopArticle();
    }

    @GetMapping("/{articleId}")
    public ResponseResult getArticleDetail(@PathVariable("articleId")String articleId){
        return articleService.getArticle(articleId, Constants.YES_CACHE);
    }

    /**
     * 获取推荐文章
     * 通过标签来返回相应文章
     * 每一次随机取出来该文章的某一个标签数
     * 然后返回，在这个标签下的item
     * 如果标签中没有文章，就返回最新的文章
     * @param articleId
     * @return
     */
    @GetMapping("/recommend/{articleId}/{size}")
    public ResponseResult getRecommendArticle(@PathVariable("articleId")String articleId, @PathVariable("size")int size){
        return articleService.getRecommendArticle(articleId, size);
    }

    @GetMapping("/label/{label}/{page}/{size}")
    public ResponseResult getArticleByLabel(@PathVariable("label")String label,
                                            @PathVariable("page")int page, @PathVariable("size")int size){
        return articleService.listArticles(page, size, null, null, Constants.Article.STATE_PUBLISH, label, Constants.No_CACHE, null);
    }

    /**
     * 获取标签云，count是权重
     * @param size
     * @return
     */
    @GetMapping("label/{size}")
    public ResponseResult getLabels(@PathVariable("size")int size){
        return articleService.getLabels(size);
    }
}
