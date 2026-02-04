package com.imis.petservicebackend.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    private Long id;

    private String username;

    private String password;

    private String phone;

    private String avatar;

    private Integer role;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private String address;
}