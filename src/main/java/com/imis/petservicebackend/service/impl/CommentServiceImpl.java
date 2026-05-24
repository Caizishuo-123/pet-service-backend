package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.Comment;
import com.imis.petservicebackend.entity.CommunityPost;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.mapper.CommunityPostMapper;
import com.imis.petservicebackend.service.CommentService;
import com.imis.petservicebackend.service.UserService;
import com.imis.petservicebackend.mapper.CommentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 64360
 * @description 针对表【comment(评论表)】的数据库操作Service实现
 * @createDate 2026-03-02 00:01:17
 */
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Autowired
    private UserService userService;

    @Autowired
    private CommunityPostMapper communityPostMapper;

    @Override
    public boolean addComment(Long userId, Comment comment) {
        if (comment == null || comment.getPostId() == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        CommunityPost post = communityPostMapper.selectById(comment.getPostId());
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("帖子不存在或已被删除");
        }
        String content = comment.getContent() == null ? "" : comment.getContent().trim();
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 255) {
            throw new BusinessException("评论内容不能超过255个字符");
        }
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setStatus(1); // 状态正常
        return this.save(comment);
    }

    @Override
    public Page<Map<String, Object>> getCommentPage(Long postId, Integer page, Integer pageSize) {
        Page<Comment> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();

        // 只查正常状态的评论
        queryWrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime);

        Page<Comment> commentPage = this.page(pageInfo, queryWrapper);

        // 转换为 Map，携带用户信息
        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(commentPage.getTotal());

        List<Map<String, Object>> records = commentPage.getRecords().stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("postId", comment.getPostId());
            map.put("userId", comment.getUserId());
            map.put("content", comment.getContent());
            map.put("createTime", comment.getCreateTime());

            // 获取评论人信息
            User user = userService.getById(comment.getUserId());
            map.put("username", user != null ? user.getUsername() : "未知用户");
            map.put("userAvatar", user != null ? user.getAvatar() : null);
            map.put("avatar", user != null ? user.getAvatar() : null);

            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public boolean deleteMyComment(Long userId, Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException("无权删除他人的评论");
        }
        if (comment.getStatus() != null && comment.getStatus() == 0) {
            return true;
        }
        LambdaUpdateWrapper<Comment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Comment::getId, commentId)
                .set(Comment::getStatus, 0);
        return this.update(updateWrapper);
    }
}
