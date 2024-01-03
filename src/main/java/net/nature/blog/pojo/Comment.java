package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {


    private String id;

    private String parentId;

    private String articleId;

    private String content;

    private String userId;

    // 1为发布正常 0为删除
    private String state = "1";

    // 0为未置顶 1为置顶
    private String top = "0";

    private Date createTime;

    private Date updateTime;

    private NatureUser natureUser;

    private List<Comment> childComment = new ArrayList<>();

}
