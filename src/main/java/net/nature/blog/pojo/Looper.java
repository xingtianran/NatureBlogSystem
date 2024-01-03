package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Looper {

  	private String id;

  	private String title;

  	private long order = 1;

  	private String state = "1";

  	private String targetUrl;

  	private String imageUrl;

  	private Date createTime;

  	private Date updateTime;

}
