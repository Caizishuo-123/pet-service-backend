package com.imis.petservicebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.imis.petservicebackend.entity.Pet;

/**
 * @description 针对表【pet(宠物表)】的数据库操作Service（用户端）
 */
public interface PetQueryService extends IService<Pet> {

  /**
   * 分页查询可领养宠物列表（status=2）
   */
  Page<Pet> getAdoptionPage(Integer type, String breed, Integer gender, Integer ageMin,
      Integer ageMax, String keyword, Integer page, Integer pageSize);

  /**
   * 获取宠物品种列表（去重）
   */
  java.util.List<String> listAdoptionBreeds(Integer type);

  /**
   * 查询我的宠物列表
   */
  Page<Pet> getMyPetPage(Long userId, Integer page, Integer pageSize);

  /**
   * 添加我的宠物
   */
  boolean addMyPet(Long userId, Pet pet);

  /**
   * 修改我的宠物（只能改自己的）
   */
  boolean updateMyPet(Long userId, Pet pet);

  /**
   * 删除我的宠物（只能删自己的）
   */
  boolean deleteMyPet(Long userId, Long petId);
}
