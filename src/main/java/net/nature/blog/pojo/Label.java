package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Label {

  	private String id;

  	private String name;

  	private long count;

  	private Date createTime;

  	private Date updateTime;
}
