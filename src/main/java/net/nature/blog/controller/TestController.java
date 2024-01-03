package net.nature.blog.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.mapper.ArticleMapper;
import net.nature.blog.mapper.CommentMapper;
import net.nature.blog.mapper.LabelMapper;
import net.nature.blog.pojo.Article;
import net.nature.blog.pojo.Comment;
import net.nature.blog.pojo.Label;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.repository.ArticleRepository;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Transactional
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private IdWorker idWorker;

    @GetMapping("/test01")
    public ResponseResult test01(){
/*        log.info("测试测试1");
        log.info("测试测试2");
        log.info("测试测试3");*/
        String captchaContent = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + "123456");
        return ResponseResult.SUCCESS().setData("Hello World").setData(captchaContent);

    }

    @GetMapping("/test02")
    public ResponseResult test02(){
/*        ResponseResult responseResult = new ResponseResult(ResponseState.FAILURE);
        NatureUser natureUser = new NatureUser();
        natureUser.setId("1");
        natureUser.setUserName("天然");
        responseResult.setData(natureUser);*/
        return ResponseResult.SUCCESS("登录成功");
    }

    @PostMapping("/label")
    public ResponseResult addLabel(@RequestBody Label label){
        label.setId(String.valueOf(idWorker.nextId()));
        label.setCreateTime(new Date());
        label.setUpdateTime(new Date());
        labelMapper.saveOne(label);
        return ResponseResult.SUCCESS("添加标签成功。");
    }

    @DeleteMapping ("/label/{labelId}")
    public ResponseResult deleteLabel(@PathVariable("labelId")String labelId){
        boolean deleteResult = labelMapper.deleteOneById(labelId);
        if (deleteResult){
            return ResponseResult.FAILURE("删除标签成功。");
        }
        return ResponseResult.SUCCESS("删除标签失败。");
    }

    @PutMapping("/label/{labelId}")
    public ResponseResult updateLabel(@PathVariable("labelId")String labelId, @RequestBody Label label){
        Label labelDb = labelMapper.findOneById(labelId);
        if (labelDb == null) {
            return ResponseResult.FAILURE("标签不存在。");
        }
        labelDb.setName(label.getName());
        labelDb.setCount(label.getCount());
        labelDb.setUpdateTime(new Date());
        labelMapper.saveOne(labelDb);
        return ResponseResult.SUCCESS("标签修改成功。");
    }

    @GetMapping("/label/{labelId}")
    public ResponseResult getLabel(@PathVariable("labelId")String labelId){
        Label label = labelMapper.findOneById(labelId);
        if (label == null) {
            return ResponseResult.FAILURE("标签不存在。");
        }
        return ResponseResult.SUCCESS("标签查询成功。").setData(label);
    }

    @GetMapping("/label/list/{page}/{size}")
    public ResponseResult listLabels(@PathVariable("page")int page, @PathVariable("size")int size){
        if (page < 1){
            page = 1;
        }
        if (size < 1){
            size = Constants.Page.DEFAULT_SIZE;
        }
        PageHelper.startPage(page, size);
        List<Label> labels = labelMapper.findAll();
        PageInfo<Label> labelPageInfo = new PageInfo<>(labels);
        return ResponseResult.SUCCESS("标签列表查询成功。").setData(labelPageInfo);
    }

/*    @GetMapping("/label/search")
    public ResponseResult doLabelSearch(@RequestParam("keyword")String keyword, @RequestParam("count")int count){
        List<Label> labels = labelDao.findAll(new Specification<Label>() {
            @Override
            public Predicate toPredicate(Root<Label> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate preName = criteriaBuilder.like(root.get("name").as(String.class), "%" + keyword + "%");
                Predicate preCount = criteriaBuilder.equal(root.get("count").as(Integer.class), count);
                Predicate preAll = criteriaBuilder.and(preName, preCount);
                return preAll;
            }
        });
        if (labels.isEmpty()){
            return ResponseResult.FAILURE("查询失败。");
        }
        return ResponseResult.SUCCESS("查询成功。").setData(labels);
    }*/

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping ("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        // 设置字体
        specCaptcha.setFont(Captcha.FONT_9);  // 有默认字体，可以不用设置
        // 设置类型，纯数字、纯字母、字母数字混合
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);

        // 验证码存入session
        // request.getSession().setAttribute("captcha", specCaptcha.text().toLowerCase());
        // 将验证码存入redis
        redisUtil.set(Constants.User.KEY_CAPTCHA_CONTENT + "123456",specCaptcha.text().toLowerCase(),60 * 10);
        // 输出图片流
        specCaptcha.out(response.getOutputStream());
    }

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private IUserService userService;

    @PostMapping("/comment")
    public ResponseResult TestComment(@RequestBody Comment comment){
        // 检查身份
        NatureUser natureUser = userService.checkNatureUser();
        if (natureUser == null) {
            return ResponseResult.FAILURE("用户未登录。");
        }
        // 3.补全comment数据
        comment.setId(String.valueOf(idWorker.nextId()));
        comment.setUserId(natureUser.getId());
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        commentMapper.saveOne(comment);
        return ResponseResult.SUCCESS("评论成功。");
    }
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @GetMapping("all")
    public ResponseResult addAllArticle() {
        elasticsearchRestTemplate.indexOps(Article.class);
        List<Article> articles = articleMapper.findAll();
        articleRepository.saveAll(articles);
        return ResponseResult.SUCCESS("导入成功");
    }
}
