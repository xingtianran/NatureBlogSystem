package net.nature.blog.services;

import net.nature.blog.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IImageService {
    ResponseResult uploadImage(MultipartFile file, String origin);

    void viewImage(HttpServletResponse response, String imageId) throws IOException;

    ResponseResult listImages(int page, int size, String origin);

    ResponseResult deleteImage(String imageId);

    ResponseResult refreshImage(String imageId);

    ResponseResult getImageOrigins();

}
