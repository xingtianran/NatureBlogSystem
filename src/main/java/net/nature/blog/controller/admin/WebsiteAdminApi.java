package net.nature.blog.controller.admin;

import net.nature.blog.interceptor.CheckTooFrequentCommit;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IWebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/website")
public class WebsiteAdminApi {

    @Autowired
    private IWebsiteService websiteService;
    /**
     * 获取网站标题
     * @return
     */
    @GetMapping("/title")
    public ResponseResult getWebsiteTitle(){
        return websiteService.getWebsiteTitle();
    }

    /**
     * 修改网站标题
     * @param title
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/title")
    public ResponseResult updateWebsiteTitle(@RequestParam("title")String title){
        return websiteService.updateWebsiteTitle(title);
    }

    /**
     * 获取网站seo信息
     * @return
     */
    @GetMapping("/seo")
    public ResponseResult getWebsiteSeo(){
        return websiteService.getWebsiteSeo();
    }

    /**
     * 修改网站seo信息
     * @param keywords
     * @param description
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/seo")
    public ResponseResult updateWebsiteSeo(@RequestParam("keywords") String keywords,
                                           @RequestParam("description")String description){
        return websiteService.updateWebsiteSeo(keywords, description);
    }

    /**
     * 获取网站统计信息
     * @return
     */
    @GetMapping("/view_count")
    public ResponseResult getWebsiteViewCount(){
        return websiteService.getWebsiteViewCount();
    }

}
