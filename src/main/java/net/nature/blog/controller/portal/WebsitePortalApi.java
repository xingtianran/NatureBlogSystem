package net.nature.blog.controller.portal;

import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.ICategoryService;
import net.nature.blog.services.IFriendLinkService;
import net.nature.blog.services.ILooperService;
import net.nature.blog.services.IWebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/website")
public class WebsitePortalApi {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IFriendLinkService friendLinkService;

    @Autowired
    private ILooperService looperService;

    @Autowired
    private IWebsiteService websiteService;

    @GetMapping("/categories")
    public ResponseResult getCategories(){
        return categoryService.listCategories();
    }

    @GetMapping("/title")
    public ResponseResult getWebsiteTitle(){
        return websiteService.getWebsiteTitle();
    }

    @GetMapping("/view_count")
    public ResponseResult getWebsiteViewCount(){
        return websiteService.getWebsiteViewCount();
    }

    @PutMapping("/view_count")
    public void updateWebsiteViewCount(){
        websiteService.updateWebsiteViewCount();
    }

    @GetMapping("/seo")
    public ResponseResult getWebsiteSeo(){
        return websiteService.getWebsiteSeo();
    }

    @GetMapping("/loops")
    public ResponseResult listLoops(){
        return looperService.listLoops();
    }

    @GetMapping("/friend_links/{page}/{size}")
    public ResponseResult listFriendLinks(@PathVariable("page")int page, @PathVariable("size") int size){
        return friendLinkService.listFriendLinks(page, size);
    }

    @GetMapping("/friend_links")
    public ResponseResult getFriendLinks(){
        return friendLinkService.listFriendLinks();
    }
}
