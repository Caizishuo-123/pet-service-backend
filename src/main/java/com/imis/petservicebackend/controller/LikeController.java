package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.LikeRecordService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@Slf4j
public class LikeController {

  @Autowired
  private LikeRecordService likeRecordService;

  @Autowired
  private UserService userService;

  /**
   * 点赞/取消点赞（toggle，需登录）
   */
  @PostMapping("/toggle")
  public Result<?> toggleLike(@RequestAttribute("account") String account,
      @RequestParam Long postId) {
    Long userId = getUserId(account);
    boolean liked = likeRecordService.toggleLike(userId, postId);
    return Result.success(liked ? "点赞成功" : "取消点赞成功");
  }

  /**
   * 是否已点赞（需登录）
   */
  @GetMapping("/status")
  public Result<?> isLiked(@RequestAttribute("account") String account,
      @RequestParam Long postId) {
    Long userId = getUserId(account);
    boolean liked = likeRecordService.isLiked(userId, postId);
    return Result.success(liked);
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
