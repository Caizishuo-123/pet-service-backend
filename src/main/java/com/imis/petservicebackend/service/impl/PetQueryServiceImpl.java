package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.Pet;
import com.imis.petservicebackend.mapper.PetMapper;
import com.imis.petservicebackend.service.PetQueryService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @description 针对表【pet(宠物表)】的数据库操作Service实现（用户端）
 */
@Service
public class PetQueryServiceImpl extends ServiceImpl<PetMapper, Pet>
    implements PetQueryService {

    @Override
    public Page<Pet> getAdoptionPage(Integer type, String breed, Integer gender, Integer ageMin,
        Integer ageMax, String keyword, Integer page, Integer pageSize) {
        Page<Pet> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();

        // 只查可领养的宠物（status=2）
        queryWrapper.eq(Pet::getStatus, 2)
            .eq(type != null, Pet::getType, type)
            .like(StringUtils.hasText(breed), Pet::getBreed, breed)
            .eq(gender != null, Pet::getGender, gender)
            .ge(ageMin != null, Pet::getAge, ageMin)
            .le(ageMax != null, Pet::getAge, ageMax)
            .and(StringUtils.hasText(keyword), wrapper -> wrapper
                .like(Pet::getName, keyword)
                .or()
                .like(Pet::getBreed, keyword)
                .or()
                .like(Pet::getDescription, keyword))
            .orderByDesc(Pet::getCreateTime);

        return this.page(pageInfo, queryWrapper);
    }

  @Override
  public List<String> listAdoptionBreeds(Integer type) {
    QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("DISTINCT breed")
        .eq("status", 2)
        .eq(type != null, "type", type)
        .isNotNull("breed")
        .ne("breed", "")
        .orderByAsc("breed");

    List<Object> raw = this.listObjs(queryWrapper);
    return raw.stream()
        .filter(Objects::nonNull)
        .map(Object::toString)
        .collect(Collectors.toList());
  }

  @Override
  public Page<Pet> getMyPetPage(Long userId, Integer page, Integer pageSize) {
    Page<Pet> pageInfo = new Page<>(page, pageSize);
    LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();

    queryWrapper.eq(Pet::getOwnerId, userId)
        .orderByDesc(Pet::getCreateTime);

    return this.page(pageInfo, queryWrapper);
  }

  @Override
  public boolean addMyPet(Long userId, Pet pet) {
    pet.setOwnerId(userId);
    pet.setSource(1); // 来源：用户拥有
    pet.setStatus(1); // 状态：用户拥有
    return this.save(pet);
  }

  @Override
  public boolean updateMyPet(Long userId, Pet pet) {
    if (pet.getId() == null) {
      throw new BusinessException("宠物ID不能为空");
    }
    // 检查是否是自己的宠物
    Pet existPet = this.getById(pet.getId());
    if (existPet == null) {
      throw new BusinessException("宠物不存在");
    }
    if (!userId.equals(existPet.getOwnerId())) {
      throw new BusinessException("无权修改他人的宠物");
    }
    // 不允许修改 source, ownerId, status
    pet.setSource(null);
    pet.setOwnerId(null);
    pet.setStatus(null);
    return this.updateById(pet);
  }

  @Override
  public boolean deleteMyPet(Long userId, Long petId) {
    Pet existPet = this.getById(petId);
    if (existPet == null) {
      throw new BusinessException("宠物不存在");
    }
    if (!userId.equals(existPet.getOwnerId())) {
      throw new BusinessException("无权删除他人的宠物");
    }
    return this.removeById(petId);
  }
}
