package net.nature.blog.services.impl;

import net.nature.blog.mapper.SettingMapper;
import net.nature.blog.pojo.Setting;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IWebsiteService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.RedisUtil;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class WebsiteServiceImpl implements IWebsiteService {

    @Autowired
    private SettingMapper settingMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IdWorker idWorker;

    @Override
    public ResponseResult getWebsiteTitle() {
        Setting setting = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_TITLE);
        return ResponseResult.SUCCESS("获取网站标题成功").setData(setting);
    }

    @Override
    public ResponseResult updateWebsiteTitle(String title) {
        // 1.检查数据
        if(TextUtils.isEmpty(title)){
            return ResponseResult.FAILURE("网站标题不能为空");
        }
        // 2.查出数据，如果没有就创建
        boolean result = false;
        Setting settingFromDb = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_TITLE);
        if (settingFromDb == null){
            Setting setting = new Setting();
            setting.setId(String.valueOf(idWorker.nextId()));
            setting.setKey(Constants.Setting.KEY_WEBSITE_TITLE);
            setting.setUpdateTime(new Date());
            setting.setCreateTime(new Date());
            setting.setValue(title);
            result = settingMapper.saveOne(setting);
        }else {
            result = settingMapper.updateValueByKey(title,Constants.Setting.KEY_WEBSITE_TITLE);
        }
        return result ? ResponseResult.SUCCESS("更新网站标题成功") : ResponseResult.FAILURE("更新网站标题失败");
    }

    @Override
    public ResponseResult getWebsiteSeo() {
        Setting description = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_DESCRIPTION);
        Setting keywords = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_KEYWORDS);
        Map<String,String> result = new HashMap<>();
        result.put(description.getKey(), description.getValue());
        result.put(keywords.getKey(), keywords.getValue());
        return ResponseResult.SUCCESS("获取网站seo信息成功").setData(result);
    }

    @Override
    public ResponseResult updateWebsiteSeo(String keywords, String description) {
        // 1.检查数据
        if (TextUtils.isEmpty(keywords)){
            return ResponseResult.FAILURE("网站关键字不能为空");
        }
        if (TextUtils.isEmpty(description)){
            return ResponseResult.FAILURE("网站描述不能为空");
        }
        // 2.如果数据库有该数据就修改，没有就添加
        boolean result = false;
        Setting descriptionFromDb = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_DESCRIPTION);
        if (descriptionFromDb == null){
            Setting setting = new Setting();
            setting.setId(String.valueOf(idWorker.nextId()));
            setting.setKey(Constants.Setting.KEY_WEBSITE_DESCRIPTION);
            setting.setValue(description);
            setting.setCreateTime(new Date());
            setting.setUpdateTime(new Date());
            result = settingMapper.saveOne(setting);
        }else {
            result = settingMapper.updateValueByKey(description, Constants.Setting.KEY_WEBSITE_DESCRIPTION);
        }
        Setting keywordsFromDb = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_KEYWORDS);
        if (keywordsFromDb == null){
            Setting setting = new Setting();
            setting.setId(String.valueOf(idWorker.nextId()));
            setting.setKey(Constants.Setting.KEY_WEBSITE_KEYWORDS);
            setting.setValue(keywords);
            setting.setCreateTime(new Date());
            setting.setUpdateTime(new Date());
            result = settingMapper.saveOne(setting);
        }else {
            result = settingMapper.updateValueByKey(keywords, Constants.Setting.KEY_WEBSITE_KEYWORDS);
        }
        return result ? ResponseResult.SUCCESS("网站seo信息更新成功") : ResponseResult.FAILURE("网站seo信息更新失败");
    }

    /**
     * 全站访问量
     * @return
     */
    @Override
    public ResponseResult getWebsiteViewCount() {
        // 在获取view_count的时候，把redis中的view_count的值存放到mysql中
        String redisViewCount = (String) redisUtil.get(Constants.Setting.KEY_WEBSITE_VIEW_COUNT);
        // 如果mysql中的view_count还没有初始化的话，就初始化
        Setting settingFromDb = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_VIEW_COUNT);
        if (settingFromDb == null){
           settingFromDb = initSetting();
           settingMapper.saveOne(settingFromDb);
        }
        // 如果redisViewCount为null，就取出mysql中，给它放进去。
        if (redisViewCount == null){
            redisViewCount = settingFromDb.getValue();
            redisUtil.set(Constants.Setting.KEY_WEBSITE_VIEW_COUNT, redisViewCount);
        }else {
            // 如果redis中有，就直接更新mysql中的
            settingMapper.updateValueByKey(redisViewCount, Constants.Setting.KEY_WEBSITE_VIEW_COUNT);
        }
        Map<String, Integer> result = new HashMap<>();
        result.put(settingFromDb.getKey(), Integer.valueOf(redisViewCount));
        return ResponseResult.SUCCESS("网站访问量获取成功").setData(result);
    }

    private Setting initSetting(){
        Setting setting = new Setting();
        setting.setId(String.valueOf(idWorker.nextId()));
        setting.setKey(Constants.Setting.KEY_WEBSITE_VIEW_COUNT);
        setting.setValue("1");
        setting.setCreateTime(new Date());
        setting.setUpdateTime(new Date());
        return setting;
    }
    /**
     * 更新redis中的view_count
     * @return
     */
    @Override
    public void updateWebsiteViewCount() {
        Object viewCount = redisUtil.get(Constants.Setting.KEY_WEBSITE_VIEW_COUNT);
        // 如果viewCount为空的话，就从mysql中取出
        if (viewCount == null) {
            Setting setting = settingMapper.findOneByKey(Constants.Setting.KEY_WEBSITE_VIEW_COUNT);
            if (setting == null){
                // 直接在这里初始化它
                setting = initSetting();
                settingMapper.saveOne(setting);
            }
            // 放入redis中
            redisUtil.set(Constants.Setting.KEY_WEBSITE_VIEW_COUNT, setting.getValue());
        }else {
            // viewCount不为空的话，要更新redis中的值
            redisUtil.incr(Constants.Setting.KEY_WEBSITE_VIEW_COUNT, 1);
        }
    }
}
