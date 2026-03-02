package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.Comment;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author 64360
 * @description 针对表【comment(评论表)】的数据库操作Service
 * @createDate 2026-03-02 00:01:17
 */
public interface CommentService extends IService<Comment> {

  /**
   * 发评论
   */
  boolean addComment(Long userId, Comment comment);

  /**
   * 查看帖子的评论列表（分页，带用户信息）
   */
  Page<Map<String, Object>> getCommentPage(Long postId, Integer page, Integer pageSize);

  /**
   * 删除我的评论（只能删自己的）
   */
  boolean deleteMyComment(Long userId, Long commentId);
}
