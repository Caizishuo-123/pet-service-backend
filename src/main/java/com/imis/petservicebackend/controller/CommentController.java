package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.Comment;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.CommentService;
import com.imis.petservicebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

  @Autowired
  private CommentService commentService;

  @Autowired
  private UserService userService;

  /**
   * 发评论（需登录）
   */
  @PostMapping("/add")
  public Result<?> addComment(@RequestAttribute("account") String account,
      @RequestBody Comment comment) {
    Long userId = getUserId(account);
    boolean flag = commentService.addComment(userId, comment);
    return flag ? Result.success("评论成功") : Result.fail("评论失败");
  }

  /**
   * 查看帖子的评论列表（游客可访问）
   */
  @GetMapping("/page")
  public Result<?> getCommentPage(
      @RequestParam Long postId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<Map<String, Object>> pageInfo = commentService.getCommentPage(postId, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 删除我的评论（需登录，只能删自己的）
   */
  @DeleteMapping("/delete/{id}")
  public Result<?> deleteComment(@RequestAttribute("account") String account,
      @PathVariable Long id) {
    Long userId = getUserId(account);
    boolean flag = commentService.deleteMyComment(userId, id);
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
