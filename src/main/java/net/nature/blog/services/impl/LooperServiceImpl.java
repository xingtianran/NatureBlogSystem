package net.nature.blog.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import net.nature.blog.mapper.LooperMapper;
import net.nature.blog.pojo.Looper;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.ILooperService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class LooperServiceImpl extends BaseService implements ILooperService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private LooperMapper looperMapper;

    @Override
    public ResponseResult addLooper(Looper looper) {
        // 1.检查数据
        if (TextUtils.isEmpty(looper.getTitle())){
            return ResponseResult.FAILURE("轮播图标题不能为空");
        }
        if (TextUtils.isEmpty(looper.getImageUrl())){
            return ResponseResult.FAILURE("轮播图图片不能为空");
        }
        if (TextUtils.isEmpty(looper.getTargetUrl())){
            return ResponseResult.FAILURE("轮播图跳转链接不能为空");
        }
        // 2.补全数据
        looper.setId(String.valueOf(idWorker.nextId()));
        looper.setCreateTime(new Date());
        looper.setUpdateTime(new Date());
        // 3.保存数据
        boolean saveResult = looperMapper.saveOne(looper);
        return saveResult ? ResponseResult.SUCCESS("轮播图保存成功") : ResponseResult.FAILURE("轮播图保存失败");
    }

    @Override
    public ResponseResult getLooper(String looperId) {
        Looper looper = looperMapper.findOneById(looperId);
        return looper != null ? ResponseResult.SUCCESS("轮播图获取成功").setData(looper) : ResponseResult.FAILURE("轮播图获取失败");
    }

    @Override
    public ResponseResult listLoops(int page, int size) {
        // 1.检查分页数据
        page = checkPage(page);
        size = checkSize(size);
        // 2.获取数据
        PageHelper.startPage(page, size);
        List<Looper> loops = looperMapper.findAll();
        PageInfo<Looper> pageInfo = new PageInfo<>(loops);
        return ResponseResult.SUCCESS("获取轮播图列表成功").setData(pageInfo);
    }

    /**
     * 前端访问的
     * @return
     */
    @Override
    public ResponseResult listLoops() {
        List<Looper> loops = looperMapper.findAllNoDelete();
        return ResponseResult.SUCCESS("获取轮播图成功").setData(loops);
    }

    @Override
    public ResponseResult updateLooper(String looperId, Looper looper) {
        // 1.查出该数据
        Looper looperFromDb = looperMapper.findOneById(looperId);
        if (looperFromDb == null){
            return ResponseResult.FAILURE("轮播图不存在");
        }
        // 2.填充来自数据库的数据
        String title = looper.getTitle();
        if (!TextUtils.isEmpty(title)){
            looperFromDb.setTitle(title);
        }
        String imageUrl = looper.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)){
            looperFromDb.setImageUrl(imageUrl);
        }
        String targetUrl = looper.getTargetUrl();
        if (!TextUtils.isEmpty(targetUrl)){
            looperFromDb.setTargetUrl(targetUrl);
        }
        String state = looper.getState();
        if (Constants.DEFAULT_STATE.equals(state) || Constants.DISABLE_STATE.equals(state)){
            looperFromDb.setState(state);
        }
        looperFromDb.setOrder(looper.getOrder());
        looperFromDb.setUpdateTime(new Date());
        // 3.修改数据库中该数据
        boolean updateResult = looperMapper.updatePartOne(looperFromDb);
        return updateResult ? ResponseResult.SUCCESS("修改轮播图数据成功") : ResponseResult.FAILURE("修改轮播图数据失败");
    }

    @Override
    public ResponseResult deleteLooper(String looperId) {
        return looperMapper.deleteLooperById(looperId) ? ResponseResult.SUCCESS("轮播图删除成功") : ResponseResult.FAILURE("轮播图删除失败");
    }
}
