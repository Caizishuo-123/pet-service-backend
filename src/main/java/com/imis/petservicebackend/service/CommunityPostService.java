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
  Page<Map<String, Object>> getPostPage(Integer category, String keyword, Integer page, Integer pageSize);

  /**
   * 热榜帖子列表（按点赞/评论排序）
   */
  Page<Map<String, Object>> getHotPostPage(Integer page, Integer pageSize);

  /**
   * 公告专区列表
   */
  Page<Map<String, Object>> getNoticePage(Integer page, Integer pageSize);

  /**
   * 我的帖子列表（分页）
   */
  Page<Map<String, Object>> getMyPostPage(Long userId, Integer category, Integer page, Integer pageSize);

  /**
   * 帖子详情（带发帖人头像/用户名）
   */
  Map<String, Object> getPostDetail(Long id);

  /**
   * 删除我的帖子（只能删自己的）
   */
  boolean deleteMyPost(Long userId, Long postId);
}
