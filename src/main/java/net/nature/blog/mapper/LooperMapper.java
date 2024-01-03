package net.nature.blog.mapper;

import net.nature.blog.pojo.Looper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LooperMapper {

    boolean saveOne(Looper looper);

    Looper findOneById(String id);

    List<Looper> findAll();

    boolean updatePartOne(Looper looperFromDb);

    boolean deleteLooperById(String id);

    List<Looper> findAllNoDelete();
}
