package com.imis.petservicebackend.mapper;

import com.imis.petservicebackend.entity.Pet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 针对表【pet(宠物表)】的数据库操作Mapper
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {

}
