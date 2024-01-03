package net.nature.blog.controller.admin;

import net.nature.blog.interceptor.CheckTooFrequentCommit;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 图片管理
 */
@RestController
@RequestMapping("/admin/image")
public class ImageAdminApi {

    @Autowired
    private IImageService imageService;

    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult uploadImage(@RequestParam("file")MultipartFile file,
                                      @RequestParam("origin")String origin){
        return imageService.uploadImage(file, origin);
    }


    @GetMapping("/{imageId}")
    public void getImage(HttpServletResponse response, @PathVariable("imageId") String imageId){
        try {
            imageService.viewImage(response, imageId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{imageId}")
    public ResponseResult deleteImage(@PathVariable("imageId")String imageId){
        return imageService.deleteImage(imageId);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listImages(@PathVariable("page")int page,
                                     @PathVariable("size")int size,
                                     @RequestParam(value = "origin", required = false)String origin){
        return imageService.listImages(page, size, origin);
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/{imageId}")
    public ResponseResult refreshImage(@PathVariable("imageId")String imageId){
        return imageService.refreshImage(imageId);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/origin")
    public ResponseResult getImageOrigins(){
        return imageService.getImageOrigins();
    }
}
