package com.imis.petservicebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName like_record
 */
@TableName(value ="like_record")
@Data
public class LikeRecord {
    private Long id;

    private Long userId;

    private Long postId;

    private Date createTime;

    private Date updateTime;
}