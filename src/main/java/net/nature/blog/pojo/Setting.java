package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Setting {


  	private String id;

  	private String key;

  	private String value;

  	private Date createTime;

  	private Date updateTime;

}
