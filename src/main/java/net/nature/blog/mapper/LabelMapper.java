package net.nature.blog.mapper;

import net.nature.blog.pojo.Label;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface LabelMapper {

    boolean deleteOneById(String id);

    Label findOneById(String id);

    boolean saveOne(Label label);

    List<Label> findAll();
    boolean updateCountByLabel(String label);

    List<Label> findPartLabel(int size);
}
