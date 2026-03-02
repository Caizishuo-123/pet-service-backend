package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.LikeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 64360
 * @description 针对表【like_record(点赞记录表)】的数据库操作Service
 * @createDate 2026-03-02 00:01:22
 */
public interface LikeRecordService extends IService<LikeRecord> {

  /**
   * 点赞/取消点赞（toggle）
   *
   * @return true=点赞成功, false=取消点赞成功
   */
  boolean toggleLike(Long userId, Long postId);

  /**
   * 是否已点赞
   */
  boolean isLiked(Long userId, Long postId);
}
