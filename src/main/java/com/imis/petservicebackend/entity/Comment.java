package com.imis.petservicebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment {
    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    private Integer status;

    private Date createTime;

    private Date updateTime;
}