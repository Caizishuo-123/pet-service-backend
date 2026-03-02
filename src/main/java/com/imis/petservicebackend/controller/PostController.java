package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.CommunityPost;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.CommunityPostService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

  @Autowired
  private CommunityPostService communityPostService;

  @Autowired
  private UserService userService;

  /**
   * 发帖（需登录）
   */
  @PostMapping("/add")
  public Result<?> createPost(@RequestAttribute("account") String account,
      @RequestBody CommunityPost post) {
    Long userId = getUserId(account);
    boolean flag = communityPostService.createPost(userId, post);
    return flag ? Result.success("发帖成功") : Result.fail("发帖失败");
  }

  /**
   * 帖子列表（游客可访问）
   */
  @GetMapping("/page")
  public Result<?> getPostPage(
      @RequestParam(required = false) Integer type,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<Map<String, Object>> pageInfo = communityPostService.getPostPage(type, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 帖子详情（游客可访问）
   */
  @GetMapping("/detail/{id}")
  public Result<?> getPostDetail(@PathVariable Long id) {
    Map<String, Object> detail = communityPostService.getPostDetail(id);
    if (detail == null) {
      return Result.fail("帖子不存在或已被屏蔽");
    }
    return Result.success(detail);
  }

  /**
   * 删除我的帖子（需登录，只能删自己的）
   */
  @DeleteMapping("/delete/{id}")
  public Result<?> deletePost(@RequestAttribute("account") String account,
      @PathVariable Long id) {
    Long userId = getUserId(account);
    boolean flag = communityPostService.deleteMyPost(userId, id);
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
