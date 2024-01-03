package net.nature.blog.controller.portal;

import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/search")
public class SearchPortalApi {

    @Autowired
    private IElasticsearchService elasticsearchService;

    @GetMapping
    public ResponseResult doSearch(@RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "category_id", required = false)String categoryId,
                                   @RequestParam(value = "label", required = false)String label,
                                   @RequestParam(value = "sort", required = false)String sort,
                                   @RequestParam("page")int page, @RequestParam("size")int size
                                   ){
        // return elasticsearchService.doSearch(keyword, categoryId, label, sort, page, size);
        return elasticsearchService.doSearch();
    }
}
