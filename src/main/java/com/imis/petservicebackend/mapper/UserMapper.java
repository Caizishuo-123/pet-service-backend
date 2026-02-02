package com.imis.petservicebackend.mapper;

import com.imis.petservicebackend.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
* @author 64360
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2026-02-02 15:22:54
* @Entity com.imis.petservicebackend.entity.User
*/
@Repository
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




