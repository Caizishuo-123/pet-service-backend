package com.imis.petservicebackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName pet
 */
@TableName(value = "pet")
@Data
public class Pet {
  private Long id;

  private String name;

  private String image;

  private Integer type;

  private String breed;

  private Integer age;

  private Integer gender;

  private Integer healthStatus;

  private String description;

  private Integer source;

  private Long ownerId;

  private Integer status;

  private Date createTime;

  private Date updateTime;
}
