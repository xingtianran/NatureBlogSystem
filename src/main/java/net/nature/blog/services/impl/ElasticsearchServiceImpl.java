package net.nature.blog.services.impl;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.pojo.Article;
import net.nature.blog.repository.ArticleRepository;
import net.nature.blog.response.ResponseResult;
import net.nature.blog.services.IElasticsearchService;
import net.nature.blog.utils.Constants;
import net.nature.blog.utils.TextUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Transactional
@Service
public class ElasticsearchServiceImpl extends BaseService implements IElasticsearchService {

    private final ArticleRepository articleRepository;

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;


    public ElasticsearchServiceImpl(ArticleRepository articleRepository, ElasticsearchRestTemplate elasticsearchRestTemplate){
        this.articleRepository = articleRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    private boolean isExists() {
        IndexCoordinates of = IndexCoordinates.of("article");
        return elasticsearchRestTemplate.indexOps(of).exists();
    }
    private void createIndex(){
        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(Article.class);
        // 创建索引
        indexOps.create();
        // 设置映射
        indexOps.putMapping(indexOps.createMapping());
        log.info("index create...");
    }
    @Override
    public ResponseResult doSearch(String keyword, String categoryId, String label, String sort, int page, int size) {
        log.info("categoryId ==>" + categoryId);
        // 查询是否拥有索引库，没有的话就创建
        boolean result = isExists();
        if (!result){
           createIndex();
        }
        // 1.检查数据
        page = checkPage(page);
        size = checkSize(size);
        //2.高亮设置
        HighlightBuilder highlightBuilder = getHighlightBuilder();
        //3.多条件查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 全文搜索keyword
        if (!TextUtils.isEmpty(keyword)){
            log.info("category entry...");
            queryBuilder.must(QueryBuilders.multiMatchQuery(keyword, "title", "content"));
        }
        // 精确查询categoryId
        if (!TextUtils.isEmpty(categoryId) && !"null".equals(categoryId)){
            queryBuilder.must(QueryBuilders.termQuery("categoryId", categoryId));
        }
        // 精确查询label
        if (!TextUtils.isEmpty(label)){
            queryBuilder.must(QueryBuilders.termQuery("label", label));
        }
        // 1按照时间正序排序，2按照时间倒序排序，3按照浏览量正序排序，4按照浏览量逆序排序
        String fieldSort = getFieldSort(sort);
        SortOrder sortOrder = getSortOrder(sort);
        // 分页
        Pageable pageable = PageRequest.of(page - 1, size);
        // 查询构造器
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        // 只有有排序属性才排序，不然按照搜索权重排序
        if (fieldSort != null && sortOrder != null){
            builder.withSort(SortBuilders.fieldSort(fieldSort).order(sortOrder));
        }
        NativeSearchQuery searchQuery = builder
                .withQuery(queryBuilder)
                .withPageable(pageable)
                .withHighlightBuilder(highlightBuilder)
                .build();
        // 搜索结果
        SearchHits<Article> searchHits = elasticsearchRestTemplate.search(searchQuery, Article.class);
        // 将高亮部分放入article对象
        List<Article> list = replaceHighlight(searchHits);
        // 分页信息
        Page<Article> pageInfo = new PageImpl<>(list, pageable, searchHits.getTotalHits());
        return ResponseResult.SUCCESS("搜索成功").setData(pageInfo);
    }

    @Override
    public ResponseResult doSearch() {
        return ResponseResult.SUCCESS("搜索成功");
    }


    /**
     * 添加文章
     * @param article
     */
    @Override
    public void addArticle(Article article) {
        // 1.处理数据
        String content = article.getContent();
        String type = article.getType();
        String html = null;
        if (Constants.Article.TYPE_MARKDOWN.equals(type)){
            // markdown转html
            html = parseMarkdownFlexmark(content);
        }else {
            html = content;
        }
        // html转text
        String text = Jsoup.parse(html).text();
        article.setContent(text);
        //保存到es
        articleRepository.save(article);
    }
    public static String parseMarkdownFlexmark(String markdown) {
        MutableDataSet options = new MutableDataSet();

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(markdown);
        String html = renderer.render(document);
        return html;
    }

    @Override
    public void deleteArticle(String articleId) {
        articleRepository.deleteById(articleId);
    }

    public void updateArticle(Article article){
        // 1.处理数据
        String content = article.getContent();
        String type = article.getType();
        String html = null;
        if (Constants.Article.TYPE_MARKDOWN.equals(type)){
            // markdown转html
            html = parseMarkdownFlexmark(content);
        }else {
            html = content;
        }
        // html转text
        String text = Jsoup.parse(html).text();
        article.setContent(text);
        articleRepository.save(article);
    }

    private HighlightBuilder getHighlightBuilder(){
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 高亮查询字段
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        // 多个字段高亮，这个要false
        highlightBuilder.requireFieldMatch(false);
        // 高亮设置
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        // 最大高亮分片数
        highlightBuilder.fragmentSize(800000);
        // 从第一个分片获取高亮分片
        highlightBuilder.numOfFragments(0);
        return highlightBuilder;
    }
    private String getFieldSort(String sort){
        if (Constants.Article.TIME_ASC.equals(sort) || Constants.Article.TIME_DESC.equals(sort)){
            return "createTime";
        }else if (Constants.Article.VIEW_COUNT_ASC.equals(sort) || Constants.Article.VIEW_COUNT_DESC.equals(sort)){
            return "viewCount";
        }
        return null;
    }
    private SortOrder getSortOrder(String sort){
        if (Constants.Article.TIME_ASC.equals(sort) || Constants.Article.VIEW_COUNT_ASC.equals(sort)){
            return SortOrder.ASC;
        }else if (Constants.Article.TIME_DESC.equals(sort) || Constants.Article.VIEW_COUNT_DESC.equals(sort)){
            return SortOrder.DESC;
        }
        return null;
    }
    private List<Article> replaceHighlight(SearchHits<Article> searchHits){
        List<Article> list = new ArrayList<>();
        // 获取到搜索到的数据
        for (SearchHit<Article> searchHit : searchHits){
            Article content = searchHit.getContent();
            // 处理高亮
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            for (Map.Entry<String, List<String>> stringHighlightFieldEntry : highlightFields.entrySet()){
                String key = stringHighlightFieldEntry.getKey();
                if (StringUtils.equals(key, "title")){
                    List<String> fragments = stringHighlightFieldEntry.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments){
                        sb.append(fragment);
                    }
                    content.setTitle(sb.toString());
                }
                if (StringUtils.equals(key, "content")){
                    List<String> fragments = stringHighlightFieldEntry.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments){
                        sb.append(fragment);
                    }
                    content.setContent(sb.toString());
                }
            }
            list.add(content);
        }
        return list;
    }
}
