package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Category {


  	private String id;

  	private String name;

  	private String pinyin;

  	private String description;

  	private long order = 1;

  	private String state = "1";

  	private Date createTime;

  	private Date updateTime;

}
