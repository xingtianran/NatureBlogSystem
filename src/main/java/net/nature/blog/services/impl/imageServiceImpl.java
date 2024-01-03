package net.nature.blog.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.mapper.ImageMapper;
import net.nature.blog.pojo.Image;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IImageService;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class imageServiceImpl extends BaseService implements IImageService {

    @Autowired
    private IUserService userService;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    public SimpleDateFormat simpleDateFormat;

    @Value("${nature.blog.image.save-path}")
    public String imagePath;

    @Value("${nature.blog.image.max-size}")
    public long maxSize;

    @Autowired
    private IdWorker idWorker;

    @Override
    public ResponseResult uploadImage(MultipartFile file, String origin) {
        // 1.判断文件是否上传
        if (file == null) {
            return ResponseResult.FAILURE("图片不可以为空");
        }
        // 2.查看文件类型
        String contentType = file.getContentType();
        if (TextUtils.isEmpty(contentType)){
            return ResponseResult.FAILURE("图片格式错误");
        }
        // 获取文件名
        String filename = file.getOriginalFilename();
        log.info("contentType ==> " + contentType);
        log.info("Filename ==> " + filename);
        // 取出后缀
        String[] splitFileName = filename.split("\\.");
        String suffix = splitFileName[splitFileName.length - 1];
        String type = null;
        // 判断图片类型 jpg、png、gif
        if ((Constants.ImageType.TYPE_JPG_WITH_PREFIX.equals(contentType) || Constants.ImageType.TYPE_JPEG_WITH_PREFIX.equals(contentType))
                && suffix.equalsIgnoreCase(Constants.ImageType.TYPE_JPG)){
            type = Constants.ImageType.TYPE_JPG;
        }else if (Constants.ImageType.TYPE_PNG_WITH_PREFIX.equals(contentType)
                && suffix.equalsIgnoreCase(Constants.ImageType.TYPE_PNG)){
            type = Constants.ImageType.TYPE_PNG;
        }else if (Constants.ImageType.TYPE_GIF_WITH_PREFIX.equals(contentType)
                && suffix.equalsIgnoreCase(Constants.ImageType.TYPE_GIF)){
            type = Constants.ImageType.TYPE_GIF;
        }else {
            return ResponseResult.FAILURE("图片类型不正确");
        }
        // 限制文件大小
        long size = file.getSize();
        if (size > maxSize){
            return ResponseResult.FAILURE("文件大小超出范围，最大仅支持" + maxSize/1024/1024/2 + "Mb");
        }
        // 建立文件保存路径，以及文件名
        // 路径/日期/类型/id.类型
        long currentTimeMillis = System.currentTimeMillis();
        String currentDate = simpleDateFormat.format(currentTimeMillis);
        String dayPath = imagePath + File.separator + currentDate;
        // 判断日期文件夹是否存在，如果不存在，就创建
        File dayFile = new File(dayPath);
        if (!dayFile.exists()){
            dayFile.mkdirs();
        }
        String targetName = String.valueOf(idWorker.nextId());
        String targetPath = dayPath + File.separator + type + File.separator + targetName + "." + type;
        // 判断目标文件的父文件是否存在，如果不存在就创建，类型目录
        File targetFile = new File(targetPath);
        log.info("targetFile ==> " + targetPath);
        if (!targetFile.getParentFile().exists()){
            targetFile.getParentFile().mkdirs();
        }
        // 保存文件
        try {
            // 判断文件是否存在，不存在就创建
            if (!targetFile.exists()){
                targetFile.createNewFile();
            }
            file.transferTo(targetFile);
            // 返回前端数据
            String resultPath = currentTimeMillis + "_" + targetName + "." + type;
            Map<String, String> result = new HashMap<>();
            result.put("id", resultPath);
            result.put("name", filename);
            // 保存到数据库
            Image image = new Image();
            image.setId(targetName);
            image.setUrl(resultPath);
            image.setName(filename);
            image.setPath(targetPath);
            image.setContentType(contentType);
            image.setOrigin(origin);
            image.setState("1");
            image.setCreateTime(new Date());
            image.setUpdateTime(new Date());
            NatureUser natureUser = userService.checkNatureUser();
            image.setUserId(natureUser.getId());
            imageMapper.saveOne(image);
            return ResponseResult.SUCCESS("图片上传成功").setData(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.FAILURE("图片上传失败，请重试");
    }

    @Override
    public void viewImage(HttpServletResponse response, String imageId) throws IOException {
            if ("undefined".equals(imageId)){
                return;
            }
            // 1698154784283_1166491241295118336.png
            String[] splitPath = imageId.split("_");
            String dateValue = splitPath[0];
            String datePath = new SimpleDateFormat("yyyy-MM-dd").format(Long.parseLong(dateValue));
            log.info("datePath ==> " + datePath);
            String[] splitNameAndType = splitPath[1].split("\\.");
            String fileType = splitNameAndType[splitNameAndType.length - 1];
            log.info("fileType ==> " + fileType);
            log.info("fileName ==> " + splitPath[splitPath.length - 1]);
            String targetPath = imagePath + File.separator + datePath + File.separator + fileType + File.separator + splitPath[splitPath.length -1];
            log.info("targetPath ==> " + targetPath);
            File file = new File(targetPath);
            OutputStream writer = null;
            FileInputStream fis = null;
            try {
                response.setContentType(Constants.ImageType.PREFIX + fileType);
                writer = response.getOutputStream();
                // 读取
                fis = new FileInputStream(file);
                byte[] buff = new byte[1024];
                int len;
                while ((len = fis.read(buff)) != -1){
                    writer.write(buff, 0, len);
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (fis != null){
                    fis.close();
                }
                if (writer != null){
                    writer.close();
                }
            }
    }

    @Override
    public ResponseResult listImages(int page, int size, String origin) {
        NatureUser natureUser = userService.checkNatureUser();
        // 1.检查分页数据
        page = super.checkPage(page);
        size = super.checkSize(size);
        // 2.开始查询
        PageHelper.startPage(page, size);
        List<Image> images = imageMapper.findAllByUserId(natureUser.getId(),origin);
        PageInfo<Image> pageInfo = new PageInfo<>(images);
        return ResponseResult.SUCCESS("获取图片列表成功").setData(pageInfo);
    }

    @Override
    public ResponseResult deleteImage(String imageId) {
        return imageMapper.deleteImageByUpdateState(imageId) ? ResponseResult.SUCCESS("删除成功") : ResponseResult.FAILURE("删除失败");
    }

    @Override
    public ResponseResult refreshImage(String imageId) {
        return imageMapper.refreshImage(imageId) ? ResponseResult.SUCCESS("恢复成功") : ResponseResult.FAILURE("恢复失败");
    }

    @Override
    public ResponseResult getImageOrigins() {
        List<String> origins = imageMapper.getImageOrigins();
        return ResponseResult.SUCCESS("获取全部来源成功").setData(origins);
    }
}
