package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.entity.Comment;
import com.imis.petservicebackend.entity.CommunityPost;
import com.imis.petservicebackend.entity.User;
import com.imis.petservicebackend.service.CommentService;
import com.imis.petservicebackend.service.CommunityPostService;
import com.imis.petservicebackend.service.UserService;
import com.imis.petservicebackend.mapper.CommunityPostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
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

    @Autowired
    private CommentService commentService;

    @Override
    public boolean createPost(Long userId, CommunityPost post) {
        Integer category = post.getCategory();
        if (category == null) {
            // 兼容旧前端：type 之前承载分类
            category = post.getType();
        }
        if (category == null || category < 1 || category > 5) {
            category = 1;
        }
        post.setUserId(userId);
        post.setType(1); // 用户端仅允许普通帖
        post.setCategory(category);
        post.setLikeCount(0); // 初始点赞数 0
        post.setStatus(1); // 状态正常
        return this.save(post);
    }

    @Override
    public Page<Map<String, Object>> getPostPage(Integer category, String keyword, Integer page, Integer pageSize) {
        Page<CommunityPost> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();

        // 只查正常状态的帖子
        // .eq(CommunityPost::getType, 1)
        queryWrapper.eq(CommunityPost::getStatus, 1)
                .eq(category != null, CommunityPost::getCategory, category)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(CommunityPost::getTitle, keyword)
                        .or()
                        .like(CommunityPost::getContent, keyword))
                .orderByDesc(CommunityPost::getCreateTime);

        Page<CommunityPost> postPage = this.page(pageInfo, queryWrapper);

        // 转换为 Map，携带用户信息
        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(postPage.getTotal());

        List<Map<String, Object>> records = postPage.getRecords().stream().map(post -> {
            User user = userService.getById(post.getUserId());
            return buildPostMap(post, user);
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Page<Map<String, Object>> getHotPostPage(Integer page, Integer pageSize) {
        Page<CommunityPost> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(CommunityPost::getStatus, 1)
                .eq(CommunityPost::getType, 1)
                .orderByDesc(CommunityPost::getLikeCount)
                .orderByDesc(CommunityPost::getCreateTime);

        Page<CommunityPost> postPage = this.page(pageInfo, queryWrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(postPage.getTotal());

        List<Map<String, Object>> records = postPage.getRecords().stream().map(post -> {
            User user = userService.getById(post.getUserId());
            return buildPostMap(post, user);
        }).collect(Collectors.toList());

        records.sort(Comparator
                .comparing((Map<String, Object> item) -> toInt(item.get("likeCount"))).reversed()
                .thenComparing(item -> toInt(item.get("commentCount")), Comparator.reverseOrder())
                .thenComparing(item -> (java.util.Date) item.get("createTime"), Comparator.nullsLast(Comparator.reverseOrder())));

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Page<Map<String, Object>> getNoticePage(Integer page, Integer pageSize) {
        Page<CommunityPost> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(CommunityPost::getStatus, 1)
                .eq(CommunityPost::getType, 2)
                .orderByDesc(CommunityPost::getCreateTime);

        Page<CommunityPost> postPage = this.page(pageInfo, queryWrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(postPage.getTotal());

        List<Map<String, Object>> records = postPage.getRecords().stream().map(post -> {
            User user = userService.getById(post.getUserId());
            return buildPostMap(post, user);
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Page<Map<String, Object>> getMyPostPage(Long userId, Integer category, Integer page, Integer pageSize) {
        Page<CommunityPost> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(CommunityPost::getStatus, 1)
                .eq(CommunityPost::getUserId, userId)
                .eq(CommunityPost::getType, 1)
                .eq(category != null, CommunityPost::getCategory, category)
                .orderByDesc(CommunityPost::getCreateTime);

        Page<CommunityPost> postPage = this.page(pageInfo, queryWrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(postPage.getTotal());

        List<Map<String, Object>> records = postPage.getRecords().stream().map(post -> {
            User user = userService.getById(post.getUserId());
            return buildPostMap(post, user);
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

        User user = userService.getById(post.getUserId());
        return buildPostMap(post, user);
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

    private Map<String, Object> buildPostMap(CommunityPost post, User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("userId", post.getUserId());
        map.put("type", post.getType());
        map.put("category", post.getCategory());
        map.put("title", post.getTitle());
        map.put("content", post.getContent());
        map.put("imageUrl", post.getImageUrl());
        map.put("likeCount", post.getLikeCount());
        map.put("createTime", post.getCreateTime());

        int commentCount = Math.toIntExact(commentService.count(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, post.getId())
                .eq(Comment::getStatus, 1)));
        map.put("commentCount", commentCount);

        if (user != null) {
            map.put("username", user.getUsername());
            map.put("userAvatar", user.getAvatar());
            map.put("avatar", user.getAvatar());
        } else {
            map.put("username", "未知用户");
            map.put("userAvatar", null);
            map.put("avatar", null);
        }

        return map;
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
