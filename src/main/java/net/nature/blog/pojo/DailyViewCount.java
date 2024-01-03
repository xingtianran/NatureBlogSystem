package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class DailyViewCount {


  	private String id;

  	private long viewCount;

  	private Date createTime;

  	private Date updateTime;

}
