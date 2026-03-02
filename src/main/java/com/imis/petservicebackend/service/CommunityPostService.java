package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.CommunityPost;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author 64360
 * @description 针对表【community_post(社区帖子表)】的数据库操作Service
 * @createDate 2026-03-02 00:01:20
 */
public interface CommunityPostService extends IService<CommunityPost> {

  /**
   * 发帖（用户端 type=1 普通帖）
   */
  boolean createPost(Long userId, CommunityPost post);

  /**
   * 帖子列表（分页，带发帖人信息）
   */
  Page<Map<String, Object>> getPostPage(Integer type, Integer page, Integer pageSize);

  /**
   * 帖子详情（带发帖人头像/用户名）
   */
  Map<String, Object> getPostDetail(Long id);

  /**
   * 删除我的帖子（只能删自己的）
   */
  boolean deleteMyPost(Long userId, Long postId);
}
