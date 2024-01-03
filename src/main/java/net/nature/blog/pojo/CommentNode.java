package net.nature.blog.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CommentNode {
    private String id;
    private String parentId;
    private String articleId;
    private String content;
    private String userId;

    private List<CommentNode> commentNextNode = new ArrayList<>();
    public CommentNode(){

    }

    public static boolean addNode(List<CommentNode> commentNodeList,CommentNode commentNode){
        // 只能允许有一个后继节点
        for (CommentNode commentNodeFromList : commentNodeList) {
            if (commentNodeFromList.getId().equals(commentNode.getParentId())){
                // 存放到指针的后继节点中
                commentNodeFromList.getCommentNextNode().add(commentNode);
                return true;
            }else {
                if (!commentNodeFromList.getCommentNextNode().isEmpty()) {
                    if (CommentNode.addNode(commentNodeFromList.getCommentNextNode(),commentNode)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<CommentNode> addChildNode(List<CommentNode> commentNodeList, List<Comment> childComments){
        while (!childComments.isEmpty()){
            // 取出子评论，放如父评论Node下
            int size = childComments.size();
            for (int i = 0; i < size; i++){
                if (CommentNode.addNode(commentNodeList, new CommentNode(childComments.get(i)))) {
                    childComments.remove(i);
                    i--;
                    size--;
                }
            }
        }
        return commentNodeList;
    }
    public CommentNode(Comment comment){
        this.id  = comment.getId();
        this.parentId = comment.getParentId();
        this.articleId = comment.getArticleId();
        this.content = comment.getContent();
        this.userId = comment.getUserId();
    }
}
