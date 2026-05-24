package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.AdoptionApply;
import com.imis.petservicebackend.entity.Pet;
import com.imis.petservicebackend.mapper.AdoptionApplyMapper;
import com.imis.petservicebackend.mapper.PetMapper;
import com.imis.petservicebackend.service.PetQueryService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @description 针对表【pet(宠物表)】的数据库操作Service实现（用户端）
 */
@Service
public class PetQueryServiceImpl extends ServiceImpl<PetMapper, Pet>
    implements PetQueryService {

    @Autowired
    private AdoptionApplyMapper adoptionApplyMapper;

    @Override
    public Page<Pet> getAdoptionPage(Integer type, String breed, Integer gender, Integer ageMin,
        Integer ageMax, BigDecimal priceMin, BigDecimal priceMax, String keyword, Integer page, Integer pageSize) {
        Page<Pet> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();

        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            BigDecimal temp = priceMin;
            priceMin = priceMax;
            priceMax = temp;
        }

        // 只查可领养的宠物（status=2）
        queryWrapper.eq(Pet::getStatus, STATUS_ADOPTABLE)
            .eq(type != null, Pet::getType, type)
            .like(StringUtils.hasText(breed), Pet::getBreed, breed)
            .eq(gender != null, Pet::getGender, gender)
            .ge(ageMin != null, Pet::getAge, ageMin)
            .le(ageMax != null, Pet::getAge, ageMax)
            .ge(priceMin != null, Pet::getAdoptionFee, priceMin)
            .le(priceMax != null, Pet::getAdoptionFee, priceMax)
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
        .eq("status", STATUS_ADOPTABLE)
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
  public Pet addMyPet(Long userId, Pet pet) {
    pet.setOwnerId(userId);
    pet.setSource(1); // 来源：用户拥有
    pet.setStatus(STATUS_OWNED); // 状态：用户拥有
    normalizeAdoptionFee(pet);
    if (!this.save(pet)) {
      throw new BusinessException("添加失败");
    }
    return pet;
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
    validateAdoptionFee(pet.getAdoptionFee());
    return this.updateById(pet);
  }

  @Override
  public boolean deleteMyPet(Long userId, Long petId) {
    Pet existPet = getOwnedPet(userId, petId);
    if (existPet.getStatus() == STATUS_ADOPTABLE) {
      LambdaUpdateWrapper<AdoptionApply> rejectWrapper = new LambdaUpdateWrapper<>();
      rejectWrapper.eq(AdoptionApply::getPetId, petId)
          .eq(AdoptionApply::getStatus, 1)
          .set(AdoptionApply::getStatus, 3);
      adoptionApplyMapper.update(null, rejectWrapper);
    }
    if (existPet.getStatus() == STATUS_LOCKED || existPet.getStatus() == STATUS_ADOPTION_FINISHED) {
      throw new BusinessException("领养已完成的宠物需要保留记录，不能删除");
    }
    if (existPet.getStatus() == STATUS_BLOCKED) {
      return true;
    }
    Pet updatePet = new Pet();
    updatePet.setId(petId);
    updatePet.setStatus(STATUS_BLOCKED);
    return this.updateById(updatePet);
  }

  @Override
  public boolean submitPetForAdoption(Long userId, Long petId, BigDecimal adoptionFee) {
    Pet pet = getOwnedPet(userId, petId);
    if (pet.getStatus() == STATUS_PENDING_REVIEW) {
      throw new BusinessException("该宠物已提交送养审核，请勿重复提交");
    }
    if (pet.getStatus() == STATUS_ADOPTABLE) {
      throw new BusinessException("该宠物当前已在领养列表中");
    }
    if (pet.getStatus() == STATUS_LOCKED) {
      throw new BusinessException("该宠物已有通过的领养申请，正在待支付");
    }
    if (pet.getStatus() == STATUS_BLOCKED) {
      throw new BusinessException("该宠物已删除，不能提交送养");
    }
    if (pet.getStatus() == STATUS_ADOPTION_FINISHED) {
      throw new BusinessException("该宠物已完成送养，不能再次提交送养");
    }
    validateAdoptionFee(adoptionFee);
    BigDecimal finalAdoptionFee = adoptionFee == null ? defaultAdoptionFee(pet.getAdoptionFee())
        : adoptionFee;

    Pet updatePet = new Pet();
    updatePet.setId(petId);
    updatePet.setAdoptionFee(finalAdoptionFee);
    updatePet.setStatus(STATUS_PENDING_REVIEW);
    return this.updateById(updatePet);
  }

  @Override
  public boolean cancelAdoptionSubmission(Long userId, Long petId) {
    Pet pet = getOwnedPet(userId, petId);
    if (pet.getStatus() != STATUS_PENDING_REVIEW && pet.getStatus() != STATUS_ADOPTABLE) {
      throw new BusinessException("当前宠物不在送养流程中，无法取消送养");
    }
    if (pet.getStatus() == STATUS_ADOPTABLE) {
      LambdaUpdateWrapper<AdoptionApply> rejectWrapper = new LambdaUpdateWrapper<>();
      rejectWrapper.eq(AdoptionApply::getPetId, petId)
          .eq(AdoptionApply::getStatus, 1)
          .set(AdoptionApply::getStatus, 3);
      adoptionApplyMapper.update(null, rejectWrapper);
    }

    Pet updatePet = new Pet();
    updatePet.setId(petId);
    updatePet.setStatus(STATUS_OWNED);
    return this.updateById(updatePet);
  }

  private Pet getOwnedPet(Long userId, Long petId) {
    Pet existPet = this.getById(petId);
    if (existPet == null) {
      throw new BusinessException("宠物不存在");
    }
    if (!userId.equals(existPet.getOwnerId())) {
      throw new BusinessException("无权操作他人的宠物");
    }
    return existPet;
  }

  private void normalizeAdoptionFee(Pet pet) {
    validateAdoptionFee(pet.getAdoptionFee());
    pet.setAdoptionFee(defaultAdoptionFee(pet.getAdoptionFee()));
  }

  private BigDecimal defaultAdoptionFee(BigDecimal adoptionFee) {
    return adoptionFee == null ? BigDecimal.ZERO : adoptionFee;
  }

  private void validateAdoptionFee(BigDecimal adoptionFee) {
    if (adoptionFee != null && adoptionFee.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException("领养费用不能小于0");
    }
  }
}
