package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.Pet;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.PetQueryService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pet")
@Slf4j
public class PetController {

  @Autowired
  private PetQueryService petQueryService;

  @Autowired
  private UserService userService;

  /**
   * 分页浏览可领养宠物列表（游客可访问）
   */
  @GetMapping("/adoption/page")
  public Result<?> getAdoptionPage(
      @RequestParam(required = false) Integer type,
      @RequestParam(required = false) String breed,
      @RequestParam(required = false) Integer gender,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<Pet> pageInfo = petQueryService.getAdoptionPage(type, breed, gender, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 宠物详情（游客可访问）
   */
  @GetMapping("/detail/{id}")
  public Result<?> getPetById(@PathVariable Long id) {
    Pet pet = petQueryService.getById(id);
    if (pet == null) {
      return Result.fail("宠物不存在");
    }
    return Result.success(pet);
  }

  /**
   * 我的宠物列表（需登录）
   */
  @GetMapping("/mine")
  public Result<?> getMyPets(
      @RequestAttribute("account") String account,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Long userId = getUserId(account);
    Page<Pet> pageInfo = petQueryService.getMyPetPage(userId, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 添加我的宠物（需登录）
   */
  @PostMapping("/add")
  public Result<?> addPet(@RequestAttribute("account") String account,
      @RequestBody Pet pet) {
    Long userId = getUserId(account);
    boolean flag = petQueryService.addMyPet(userId, pet);
    return flag ? Result.success("添加成功") : Result.fail("添加失败");
  }

  /**
   * 修改我的宠物（需登录，只能改自己的）
   */
  @PutMapping("/update")
  public Result<?> updatePet(@RequestAttribute("account") String account,
      @RequestBody Pet pet) {
    Long userId = getUserId(account);
    boolean flag = petQueryService.updateMyPet(userId, pet);
    return flag ? Result.success("修改成功") : Result.fail("修改失败");
  }

  /**
   * 删除我的宠物（需登录，只能删自己的）
   */
  @DeleteMapping("/delete/{id}")
  public Result<?> deletePet(@RequestAttribute("account") String account,
      @PathVariable Long id) {
    Long userId = getUserId(account);
    boolean flag = petQueryService.deleteMyPet(userId, id);
    return flag ? Result.success("删除成功") : Result.fail("删除失败");
  }

  /**
   * 根据用户名获取用户ID
   */
  private Long getUserId(String account) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getUsername, account);
    User user = userService.getOne(wrapper);
    if (user == null) {
      throw new BusinessException("用户不存在");
    }
    return user.getId();
  }
}
