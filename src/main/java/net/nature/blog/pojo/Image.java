package net.nature.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Image {


	private String id;

	private String userId;

	private String url;

	private String name;

	private String path;

	private String contentType;

	private String origin;

	private String state;

	private Date createTime;

	private Date updateTime;
}