package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "article", shards = 1, replicas = 0)
@Mapping(mappingPath = "mapper/Document.json")
public class Article {


    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(analyzer = "ik_smart", searchAnalyzer = "ik_smart", type = FieldType.Text)
    private String title;

    // @Transient 注解 不往elasticsearch中添加该数据
    @Transient
    private String userId;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(analyzer = "ik_smart", searchAnalyzer = "ik_smart", type = FieldType.Text)
    private String content;

    @Transient
    // 0表示富文本，1表示markdown
    private String type;

    @Transient
    // 0表示删除 1表示已经发布 2表示草稿
    private String state = "1";

    @Transient
    // 0表示没有置顶，1表示已置顶
    private String top = "0";

    @Transient
    private String cover;

    @Transient
    private String summary;

    @Field(analyzer = "ik_smart", searchAnalyzer = "ik_smart", type = FieldType.Text)
    private String label;

    @Field(type = FieldType.Long)
    private long viewCount = 0L;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Transient
    private Date updateTime;

    @Transient
    private NatureUser natureUser;

    @Transient
    private List<String> labels = new ArrayList<>();

    public void setLabel(String label) {
        this.label = label;
        if (!this.label.contains("-")) {
            this.labels.add(this.label);
        } else {
            List<String> labelList = Arrays.asList(this.label.split("-"));
            this.labels.addAll(labelList);
        }
    }
}
