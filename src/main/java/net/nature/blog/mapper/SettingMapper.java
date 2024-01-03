package net.nature.blog.mapper;

import net.nature.blog.pojo.Setting;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SettingMapper {
    Setting findOneByKey(String key);

    boolean saveOne(Setting setting);

    boolean updateValueByKey(String value, String key);
}
