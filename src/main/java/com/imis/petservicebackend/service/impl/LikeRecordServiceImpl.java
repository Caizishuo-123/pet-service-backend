package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.CommunityPost;
import com.imis.petservicebackend.entity.LikeRecord;
import com.imis.petservicebackend.service.CommunityPostService;
import com.imis.petservicebackend.service.LikeRecordService;
import com.imis.petservicebackend.mapper.LikeRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 64360
 * @description 针对表【like_record(点赞记录表)】的数据库操作Service实现
 * @createDate 2026-03-02 00:01:22
 */
@Service
@Slf4j
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord>
        implements LikeRecordService {

    @Autowired
    private CommunityPostService communityPostService;

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        CommunityPost post = communityPostService.getById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("帖子不存在或已被删除");
        }
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getPostId, postId)
                .last("limit 1");
        LikeRecord existRecord = this.getOne(wrapper);

        if (existRecord != null) {
            // 已点赞 → 取消点赞
            this.remove(new LambdaQueryWrapper<LikeRecord>()
                    .eq(LikeRecord::getUserId, userId)
                    .eq(LikeRecord::getPostId, postId));
            // 点赞数 -1
            refreshPostLikeCount(postId);
            return false; // 表示取消了点赞
        } else {
            // 未点赞 → 点赞
            LikeRecord record = new LikeRecord();
            record.setUserId(userId);
            record.setPostId(postId);
            this.save(record);
            // 点赞数 +1
            refreshPostLikeCount(postId);
            return true; // 表示点赞成功
        }
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getPostId, postId);
        return this.count(wrapper) > 0;
    }

    /**
     * 更新帖子点赞数
     */
    private void refreshPostLikeCount(Long postId) {
        CommunityPost post = communityPostService.getById(postId);
        if (post != null) {
            int newCount = Math.toIntExact(this.count(new LambdaQueryWrapper<LikeRecord>()
                    .eq(LikeRecord::getPostId, postId)));
            LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CommunityPost::getId, postId)
                    .set(CommunityPost::getLikeCount, newCount);
            communityPostService.update(updateWrapper);
        }
    }
}
