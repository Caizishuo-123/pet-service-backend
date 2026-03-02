package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.AdoptionApply;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.AdoptionApplyService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/adoption")
@Slf4j
public class AdoptionController {

  @Autowired
  private AdoptionApplyService adoptionApplyService;

  @Autowired
  private UserService userService;

  /**
   * 提交领养申请
   */
  @PostMapping("/apply")
  public Result<?> submitApply(@RequestAttribute("account") String account,
      @RequestBody AdoptionApply apply) {
    Long userId = getUserId(account);
    boolean flag = adoptionApplyService.submitApply(userId, apply);
    return flag ? Result.success("申请提交成功，请等待审核") : Result.fail("申请提交失败");
  }

  /**
   * 我的申请列表
   */
  @GetMapping("/mine")
  public Result<?> getMyApplies(
      @RequestAttribute("account") String account,
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Long userId = getUserId(account);
    Page<Map<String, Object>> pageInfo = adoptionApplyService
        .getMyApplyPage(userId, status, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 申请详情
   */
  @GetMapping("/detail/{id}")
  public Result<?> getApplyDetail(@PathVariable Long id) {
    Map<String, Object> detail = adoptionApplyService.getApplyDetail(id);
    if (detail == null) {
      return Result.fail("申请记录不存在");
    }
    return Result.success(detail);
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
