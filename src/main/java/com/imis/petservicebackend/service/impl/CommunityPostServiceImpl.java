package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.CommunityPost;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.CommunityPostService;
import com.imis.petservicebackend.service.UserService;
import com.imis.petservicebackend.mapper.CommunityPostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 64360
 * @description 针对表【community_post(社区帖子表)】的数据库操作Service实现
 * @createDate 2026-03-02 00:01:20
 */
@Service
@Slf4j
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost>
        implements CommunityPostService {

    @Autowired
    private UserService userService;

    @Override
    public boolean createPost(Long userId, CommunityPost post) {
        post.setUserId(userId);
        post.setType(1); // 用户端发帖 type=1（普通帖）
        post.setLikeCount(0); // 初始点赞数 0
        post.setStatus(1); // 状态正常
        return this.save(post);
    }

    @Override
    public Page<Map<String, Object>> getPostPage(Integer type, Integer page, Integer pageSize) {
        Page<CommunityPost> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();

        // 只查正常状态的帖子
        queryWrapper.eq(CommunityPost::getStatus, 1)
                .eq(type != null, CommunityPost::getType, type)
                .orderByDesc(CommunityPost::getCreateTime);

        Page<CommunityPost> postPage = this.page(pageInfo, queryWrapper);

        // 转换为 Map，携带用户信息
        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(postPage.getTotal());

        List<Map<String, Object>> records = postPage.getRecords().stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("userId", post.getUserId());
            map.put("type", post.getType());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("imageUrl", post.getImageUrl());
            map.put("likeCount", post.getLikeCount());
            map.put("createTime", post.getCreateTime());

            // 获取发帖人信息
            User user = userService.getById(post.getUserId());
            map.put("username", user != null ? user.getUsername() : "未知用户");
            map.put("userAvatar", user != null ? user.getAvatar() : null);

            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Map<String, Object> getPostDetail(Long id) {
        CommunityPost post = this.getById(id);
        if (post == null || post.getStatus() != 1) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("userId", post.getUserId());
        map.put("type", post.getType());
        map.put("title", post.getTitle());
        map.put("content", post.getContent());
        map.put("imageUrl", post.getImageUrl());
        map.put("likeCount", post.getLikeCount());
        map.put("createTime", post.getCreateTime());

        // 获取发帖人信息
        User user = userService.getById(post.getUserId());
        if (user != null) {
            map.put("username", user.getUsername());
            map.put("userAvatar", user.getAvatar());
        }

        return map;
    }

    @Override
    public boolean deleteMyPost(Long userId, Long postId) {
        CommunityPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException("无权删除他人的帖子");
        }
        return this.removeById(postId);
    }
}
