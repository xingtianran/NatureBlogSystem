package net.nature.blog.services;

import net.nature.blog.response.ResponseResult;

public interface IWebsiteService {
    ResponseResult getWebsiteTitle();

    ResponseResult updateWebsiteTitle(String title);

    ResponseResult getWebsiteSeo();

    ResponseResult updateWebsiteSeo(String keywords, String description);

    ResponseResult getWebsiteViewCount();

    void updateWebsiteViewCount();

}
