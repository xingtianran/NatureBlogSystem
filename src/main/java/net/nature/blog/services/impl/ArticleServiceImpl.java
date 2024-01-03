package net.nature.blog.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.mapper.ArticleMapper;
import net.nature.blog.mapper.CommentMapper;
import net.nature.blog.mapper.LabelMapper;
import net.nature.blog.pojo.Article;
import net.nature.blog.pojo.Label;
import net.nature.blog.pojo.NatureUser;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.response.ResponseState;
import net.nature.blog.services.IArticleService;
import net.nature.blog.services.IUserService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.RedisUtil;
import net.nature.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional
public class ArticleServiceImpl extends BaseService implements IArticleService {

    @Autowired
    private IUserService userService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private Random random;

    // @Autowired
    // private IElasticsearchService elasticsearchService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private Gson gson;

    /**
     * 以后可以整一个定时发布文章
     * @param article
     * @return
     */
    @Override
    public ResponseResult addArticle(Article article) {
        // 1.获取用户id
        NatureUser natureUser = userService.checkNatureUser();
        // 2.检查数据
        String title = article.getTitle();
        if (TextUtils.isEmpty(title)){
            return ResponseResult.FAILURE("文章标题不能为空");
        }
        // 检查提交的状态是否是发布以及草稿
        String state = article.getState();
        if (!Constants.Article.STATE_PUBLISH.equals(state)
                && !Constants.Article.STATE_DRAFT.equals(state)){
            return ResponseResult.FAILURE("文章不支持该状态");
        }
        // 判断文章类型是不是富文本and markdown
        String type = article.getType();
        if (TextUtils.isEmpty(type)){
            return ResponseResult.FAILURE("文章格式不能为空");
        }
        if (!Constants.Article.TYPE_RICH_TEXT.equals(type)
                && !Constants.Article.TYPE_MARKDOWN.equals(type)){
            return ResponseResult.FAILURE("文章不支持该格式");
        }
        // 发布时的检查，草稿的话，不需要该检查
        if (Constants.Article.STATE_PUBLISH.equals(state)){
            if (title.length() > Constants.Article.TITLE_MAX_LENGTH){
                return ResponseResult.FAILURE("文章标题不能大于" + Constants.Article.TITLE_MAX_LENGTH +"个字符" );
            }
            if (TextUtils.isEmpty(article.getContent())){
                return ResponseResult.FAILURE("文章内容不能为空");
            }
            String summary = article.getSummary();
            if (TextUtils.isEmpty(summary)){
                return ResponseResult.FAILURE("文章摘要不能为空");
            }
            if (summary.length() > Constants.Article.SUMMARY_MAX_LENGTH){
                return ResponseResult.FAILURE("文章摘要不能大于" + Constants.Article.SUMMARY_MAX_LENGTH + "个字符");
            }
            if (TextUtils.isEmpty(article.getLabel())){
                return ResponseResult.FAILURE("文章标签不能为空");
            }
        }
        // 3.补全数据，如果数据库中有这条文章记录，就更新，没有就保存,一般多次到这里只能是更新
        // 无id是发布文章，第一次保存草稿
        // 有id是非第一次保存草稿
        boolean result = false;
        String articleId = article.getId();
        if (TextUtils.isEmpty(articleId)) {
            article.setId(String.valueOf(idWorker.nextId()));
            article.setUserId(natureUser.getId());
            article.setCreateTime(new Date());
            article.setUpdateTime(new Date());
            result = articleMapper.saveOne(article);
        }else {
            Article articleFromDb = articleMapper.findOneById(articleId);
            if (Constants.Article.STATE_PUBLISH.equals(articleFromDb.getState()) &&
            Constants.Article.STATE_DRAFT.equals(article.getState())){
                return ResponseResult.FAILURE("已发布的文章不能保存为草稿");
            }
            // 更新操作，通过id更新
            article.setUpdateTime(new Date());
            result = articleMapper.updateOneById(article);
        }
        // 4.保全到数据库
        // 保存搜索数据库
        // elasticsearchService.addArticle(article);
        // 新文章加入，就删除redis中的文章
        redisUtil.del(Constants.Article.KEY_ARTICLE_FIRST_PAGE_CACHE);
        // 打散标签，保存
        this.setupLabel(article.getLabel());
        return result ? ResponseResult.SUCCESS(Constants.Article.STATE_PUBLISH.equals(state) ? "文章发布成功" : "文章保存草稿成功").setData(article.getId())
                : ResponseResult.FAILURE(Constants.Article.STATE_PUBLISH.equals(state) ? "文章发布失败" : "文章保存草稿失败").setData(article.getId());
    }

    /**
     * 将labels打散后存放到数据库
     * @param labels java-并发-协程
     */
    private void setupLabel(String labels){
        List<String> labelList = new ArrayList<>();
        if (!labels.contains("-")){
            labelList.add(labels);
        }else {
            labelList.addAll(Arrays.asList(labels.split("-")));
        }
        for (String label : labelList){
            boolean result = labelMapper.updateCountByLabel(label);
            if (!result){
                Label labelItem = new Label();
                labelItem.setId(String.valueOf(idWorker.nextId()));
                labelItem.setName(label);
                labelItem.setCount(1);
                labelItem.setCreateTime(new Date());
                labelItem.setUpdateTime(new Date());
                labelMapper.saveOne(labelItem);
            }
        }
    }
    @Override
    public ResponseResult getArticle(String articleId, String isCache) {
        // 先去redis中查，如redis中有就直接返回
        if (Constants.YES_CACHE.equals(isCache)){
            String redisArticle = (String) redisUtil.get(Constants.Article.KEY_ARTICLE_CACHE + articleId);
            if (!TextUtils.isEmpty(redisArticle)){
                // 1.增加阅读量
                redisUtil.incr(Constants.Article.KEY_VIEW_COUNT_CACHE + articleId, 1);
                // 2.将对象json转化为对象返回
                Article article = gson.fromJson(redisArticle, Article.class);
                log.info("getArticle ==> redis");
                return ResponseResult.SUCCESS("获取文章成功").setData(article);
            }
        }
        Article article = articleMapper.findOneByAndUserIdNoPassword(articleId);
        if (article == null){
            return ResponseResult.FAILURE("文章不存在");
        }
        // 判断权限
        String state = article.getState();
        if (Constants.YES_CACHE.equals(isCache)){
            if (Constants.Article.STATE_PUBLISH.equals(state)){
                // 将文章放入redis中，设置时间为5分钟,redis中的浏览量增加
                // 将markdown转成html
                String content = article.getContent();
                String type = article.getType();
                String html = null;
                if (Constants.Article.TYPE_MARKDOWN.equals(type)){
                    // markdown转html
                    html = parseMarkdownFlexmark(content);
                }else {
                    html = content;
                }
                article.setContent(html);
                String articleJson = gson.toJson(article);
                redisUtil.set(Constants.Article.KEY_ARTICLE_CACHE + articleId , articleJson,Constants.TimeValueInSecond.MIN * 5);
                String redisViewCount = (String) redisUtil.get(Constants.Article.KEY_VIEW_COUNT_CACHE + articleId);
                if (TextUtils.isEmpty(redisViewCount)){
                    // 浏览量不存在，就设置进去，加一
                    long newViewCount = article.getViewCount() + 1;
                    article.setViewCount(newViewCount);
                    redisUtil.set(Constants.Article.KEY_VIEW_COUNT_CACHE + articleId, String.valueOf(newViewCount));
                }else {
                    // 浏览量存在的话，就加一，更新到mysql
                    redisUtil.incr(Constants.Article.KEY_VIEW_COUNT_CACHE + articleId, 1);
                    long newViewCount = Long.parseLong(redisViewCount) + 1;
                    // 更新mysql
                    articleMapper.updateViewCountById(newViewCount, articleId);
                    //更新es
                    article.setViewCount(newViewCount);
                    // elasticsearchService.updateArticle(article);
                }
                log.info("getArticle ==> mysql");
                return ResponseResult.SUCCESS("获取文章成功").setData(article);
            }
        }
        // 如果是草稿或者已经删除的，只有管理员可以获得
        if (Constants.Article.STATE_DRAFT.equals(state) || Constants.Article.STATE_DELETE.equals(state)){
            NatureUser natureUser = userService.checkNatureUser();
            if (natureUser == null || !Constants.User.ROLE_ADMIN.equals(natureUser.getRoles())) {
                return ResponseResult.GET(ResponseState.PERMISSION_DENY);
            }
        }
        return ResponseResult.SUCCESS("获取文章成功").setData(article);
    }


    @Override
    public ResponseResult listArticles(int page, int size, String keyword, String categoryId, String state, String label, String isCache, String top) {
        // 1.检查数据
        page = checkPage(page);
        size = checkSize(size);
        // 2.缓存第一页文章
        // 管理中心搜索的不用缓存
        if (page == 1 && Constants.YES_CACHE.equals(isCache) && TextUtils.isEmpty(categoryId)){
            // 查看redis中是否缓存
            String redisArticle = (String) redisUtil.get(Constants.Article.KEY_ARTICLE_FIRST_PAGE_CACHE);
            if (!TextUtils.isEmpty(redisArticle)){
                PageInfo<Article> articleList = gson.fromJson(redisArticle,new TypeToken<PageInfo<Article>>() {
                }.getType());
                log.info("article from redis...");
                return ResponseResult.SUCCESS("获取文章列表成功").setData(articleList);
            }
        }
        // 3.查询
        PageHelper.startPage(page, size);
        List<Article> articles = articleMapper.findPartAllByKeywordOrCategoryIdOrLabelNoContent(keyword, categoryId, state, label, top);
        PageInfo<Article> pageInfo = new PageInfo<>(articles);
        // 如果是第一页，就缓存文章
        if (page == 1 &&  Constants.YES_CACHE.equals(isCache) && TextUtils.isEmpty(categoryId)){
            redisUtil.set(Constants.Article.KEY_ARTICLE_FIRST_PAGE_CACHE,gson.toJson(pageInfo), Constants.TimeValueInSecond.HOUR);
        }
        return ResponseResult.SUCCESS("获取文章列表成功").setData(pageInfo);
    }

    /**
     * 修改内容 标题、内容、摘要、标签、分类、封面
     * @param articleId
     * @param article
     * @return
     */
    @Override
    public ResponseResult updateArticle(String articleId, Article article) {
        // 1.查出数据，更新数据
        Article articleFromDb = articleMapper.findOneById(articleId);
        if (articleFromDb == null){
            return ResponseResult.FAILURE("文章不存在");
        }
        // 2.检查article中数据
        String title = article.getTitle();
        if (!TextUtils.isEmpty(title)){
            articleFromDb.setTitle(title);
        }
        String categoryId = article.getCategoryId();
        if (!TextUtils.isEmpty(categoryId)){
            articleFromDb.setCategoryId(categoryId);
        }
        String content = article.getContent();
        if (!TextUtils.isEmpty(content)){
            articleFromDb.setContent(content);
        }
        String label = article.getLabel();
        if (!TextUtils.isEmpty(label)){
            articleFromDb.setLabel(label);
        }
        String summary = article.getSummary();
        if (!TextUtils.isEmpty(summary)){
            articleFromDb.setSummary(summary);
        }
        articleFromDb.setCover(article.getCover());
        articleFromDb.setUpdateTime(new Date());
        // 删除redis中的缓存数据
        redisUtil.del(Constants.Article.KEY_ARTICLE_CACHE + articleId);
        // 更新el中的文章
        // elasticsearchService.updateArticle(article);
        boolean updateResult = articleMapper.updatePartOneById(articleFromDb);
        return updateResult ? ResponseResult.SUCCESS("更新文章成功") : ResponseResult.FAILURE("更新文章失败");
    }

    /**
     * 物理删除
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult deleteArticle(String articleId) {
        // 1.先删除评论才能删除文章
        commentMapper.deleteByArticleId(articleId);
        // 2.在删除文章
        boolean result = articleMapper.deleteOneById(articleId);
        if (result){
            // 3.删除redis文章和文章浏览量 删除redis中缓存的第一页文章
            deleteRedisArticle(articleId);
            // 4.删除es文章
            // elasticsearchService.deleteArticle(articleId);
            return ResponseResult.SUCCESS("删除文章成功");
        }
        return ResponseResult.FAILURE("删除文章失败");
    }

    /**
     * 删除redis文章和文章浏览量
     * @param articleId
     */
    private void deleteRedisArticle(String articleId){
        redisUtil.del(Constants.Article.KEY_ARTICLE_CACHE + articleId);
        redisUtil.del(Constants.Article.KEY_VIEW_COUNT_CACHE + articleId);
        redisUtil.del(Constants.Article.KEY_ARTICLE_FIRST_PAGE_CACHE);
    }
    @Override
    public ResponseResult deleteArticleByState(String articleId) {
        boolean result = articleMapper.deleteOneByUpdateState(articleId);
        if (result){
            // 3.删除redis文章和文章浏览量
            deleteRedisArticle(articleId);
            // 4.删除es文章
            // elasticsearchService.deleteArticle(articleId);
            return ResponseResult.SUCCESS("删除文章成功");
        }
        return ResponseResult.FAILURE("删除文章失败");
    }

    @Override
    public ResponseResult resetTopArticle(String articleId) {
        Article articleFromDb = articleMapper.findOneById(articleId);
        // 只有文章状态是已经发布的才可以置顶
        boolean result = false;
        String state = articleFromDb.getState();
        if (!Constants.Article.STATE_PUBLISH.equals(state)){
            return ResponseResult.FAILURE("该文章不可以进行此操作");
        }
        String top = articleFromDb.getTop();
        if (Constants.Article.NO_TOP.equals(top)) {
            result = articleMapper.updateOneToTop(articleId);
        }else{
            result = articleMapper.updateOneNoTop(articleId);
        }
        return result ? ResponseResult.SUCCESS(Constants.Article.NO_TOP.equals(top) ? "文章置顶成功" : "文章取消置顶成功") :
                ResponseResult.FAILURE(Constants.Article.NO_TOP.equals(top) ? "文章置顶失败" : "文章取消置顶失败");
    }

    /**
     * 获取置顶文章
     * @return
     */
    @Override
    public ResponseResult getTopArticle() {
        List<Article> topArticles = articleMapper.findAllByTop(Constants.Article.YES_TOP);
        return ResponseResult.SUCCESS("获取置顶文章成功").setData(topArticles);
    }

    /**
     * 获取推荐文章
     * @param articleId
     * @param size 页数
     * @return
     */
    @Override
    public ResponseResult getRecommendArticle(String articleId, int size) {
        // 1.查询出该文章
        Article articleFromDb = articleMapper.findOneById(articleId);
        if (articleFromDb == null){
            return ResponseResult.FAILURE("文章不存在");
        }
        // 2.随机选择一个标签
        List<String> labels = articleFromDb.getLabels();
        int labelLength = labels.size();
        List<Integer> existIndex = new ArrayList<>();
        List<Article> articleList = null;
        do{
            int randomIndex = random.nextInt(labelLength);
            if (existIndex.contains(randomIndex)) continue;
            String label = labels.get(randomIndex);
            log.info("random ==> label ==> " + label);
            existIndex.add(randomIndex);
            // 3.查询该标签下的文章
            articleList = articleMapper.findAllByLabel(label, articleId, size);
        }while (articleList.isEmpty() && existIndex.size() < labelLength);
        // 判断集合只有一个元素的情况，解决动态foreach的不足，只有一个值，调用普通sql，否则调用动态sql（foreach）
        int span = size - articleList.size();
        log.info("recommend size ==> " + articleList.size());
        List<Article> otherArticles = new ArrayList<>();
        if (articleList.size() < size){
            otherArticles = articleList.size() == 1 ? articleMapper.findNewSpecialAll(span, articleId, articleList.get(0).getId()) : articleMapper.findNewAll(span, articleId ,articleList);
        }
        articleList.addAll(otherArticles);
        return ResponseResult.SUCCESS("获取推荐文章成功").setData(articleList);
    }

    /**
     * 获取标签云
     * @param size
     * @return
     */
    @Override
    public ResponseResult getLabels(int size) {
        size = checkSize(size);
        List<Label> labels = labelMapper.findPartLabel(size);
        return ResponseResult.SUCCESS("获取标签云成功").setData(labels);
    }

    @Override
    public ResponseResult getArticleCount() {
        return ResponseResult.SUCCESS("获取文章总数成功").setData(articleMapper.getArticleCount());
    }


}
