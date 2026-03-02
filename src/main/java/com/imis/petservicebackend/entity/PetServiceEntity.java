package com.imis.petservicebackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @TableName pet_service
 */
@TableName(value = "pet_service")
@Data
public class PetServiceEntity {
  private Long id;

  private String name;

  private Integer type;

  private BigDecimal price;

  private String contactPhone;

  private Integer duration;

  private String description;

  private String imageUrl;

  private Integer sort;

  private Integer status;

  private Date createTime;

  private Date updateTime;
}
