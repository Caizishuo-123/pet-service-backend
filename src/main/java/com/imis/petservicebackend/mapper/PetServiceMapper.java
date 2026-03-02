package com.imis.petservicebackend.mapper;

import com.imis.petservicebackend.entity.PetServiceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 针对表【pet_service(宠物服务表)】的数据库操作Mapper
 */
@Mapper
public interface PetServiceMapper extends BaseMapper<PetServiceEntity> {

}
