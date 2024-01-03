package net.nature.blog.controller.admin;

import net.nature.blog.interceptor.CheckTooFrequentCommit;
import net.nature.blog.pojo.Looper;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.ILooperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/looper")
public class LooperAdminApi {

    @Autowired
    private ILooperService looperService;

    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult addLooper(@RequestBody Looper looper){
        return looperService.addLooper(looper);
    }

    @CheckTooFrequentCommit
    @PutMapping("/{looperId}")
    public ResponseResult updateLooper(@PathVariable("looperId") String looperId, @RequestBody Looper looper){
        return looperService.updateLooper(looperId, looper);
    }

    @GetMapping("/{looperId}")
    public  ResponseResult getLooper(@PathVariable("looperId") String looperId){
        return looperService.getLooper(looperId);
    }

    @DeleteMapping("/{looperId}")
    public ResponseResult deleteLooper(@PathVariable("looperId")String looperId){
        return looperService.deleteLooper(looperId);
    }

    @GetMapping("/list/{page}/{size}")
    public ResponseResult listLoops(@PathVariable("page")int page, @PathVariable("size")int size){
        return looperService.listLoops(page, size);
    }
}
