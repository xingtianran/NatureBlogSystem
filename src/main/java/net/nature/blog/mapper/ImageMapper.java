package net.nature.blog.mapper;

import net.nature.blog.pojo.Image;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageMapper {

    boolean saveOne(Image image);

    List<Image> findAllByUserId(String userId, String origin);

    boolean deleteImageByUpdateState(String Id);

    boolean refreshImage(String id);

    List<String> getImageOrigins();
}
