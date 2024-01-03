package net.nature.blog.services;

import net.nature.blog.pojo.Looper;
import net.nature.blog.response.ResponseResult;

public interface ILooperService {
    ResponseResult addLooper(Looper looper);

    ResponseResult getLooper(String looperId);

    ResponseResult listLoops(int page, int size);

    ResponseResult listLoops();
    ResponseResult updateLooper(String looperId, Looper looper);

    ResponseResult deleteLooper(String looperId);
}
