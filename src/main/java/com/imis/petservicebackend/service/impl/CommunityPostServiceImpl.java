package com.imis.petservicebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    public CommunityPost createPost(Long userId, CommunityPost post) {
        validatePostPayload(post);
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
        if (!this.save(post)) {
            throw new BusinessException("发帖失败");
        }
        return post;
    }

    @Override
    public Page<Map<String, Object>> getPostPage(Integer type, Integer category, String keyword, Integer page, Integer pageSize) {
        Page<CommunityPost> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        Integer effectiveType = type == null ? 1 : type;

        // 只查正常状态的帖子
        queryWrapper.eq(CommunityPost::getStatus, 1)
                .eq(effectiveType != 0, CommunityPost::getType, effectiveType)
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
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(CommunityPost::getStatus, 1)
                .eq(CommunityPost::getType, 1)
                .orderByDesc(CommunityPost::getLikeCount)
                .orderByDesc(CommunityPost::getCreateTime);

        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        List<Map<String, Object>> allRecords = this.list(queryWrapper).stream().map(post -> {
            User user = userService.getById(post.getUserId());
            return buildPostMap(post, user);
        }).collect(Collectors.toList());

        allRecords.sort(Comparator
                .comparing((Map<String, Object> item) -> toInt(item.get("likeCount"))).reversed()
                .thenComparing(item -> toInt(item.get("commentCount")), Comparator.reverseOrder())
                .thenComparing(item -> (java.util.Date) item.get("createTime"), Comparator.nullsLast(Comparator.reverseOrder())));

        long safePage = page == null || page < 1 ? 1 : page;
        long safePageSize = pageSize == null || pageSize < 1 ? 5 : pageSize;
        long skip = (safePage - 1) * safePageSize;
        List<Map<String, Object>> records = allRecords.stream()
                .skip(skip)
                .limit(safePageSize)
                .collect(Collectors.toList());
        resultPage.setTotal(allRecords.size());
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
    public boolean updateMyPost(Long userId, CommunityPost post) {
        if (post == null || post.getId() == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        validatePostPayload(post);
        CommunityPost existPost = this.getById(post.getId());
        if (existPost == null || existPost.getStatus() != 1) {
            throw new BusinessException("帖子不存在或已被删除");
        }
        if (!userId.equals(existPost.getUserId())) {
            throw new BusinessException("无权修改他人的帖子");
        }
        if (existPost.getType() == null || existPost.getType() != 1) {
            throw new BusinessException("只能修改普通帖子");
        }
        LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityPost::getId, post.getId())
                .set(CommunityPost::getCategory, post.getCategory())
                .set(CommunityPost::getTitle, post.getTitle().trim())
                .set(CommunityPost::getContent, post.getContent().trim())
                .set(CommunityPost::getImageUrl, StringUtils.hasText(post.getImageUrl()) ? post.getImageUrl().trim() : null);
        return this.update(updateWrapper);
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
        if (post.getStatus() != null && post.getStatus() == 0) {
            return true;
        }
        LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityPost::getId, postId)
                .set(CommunityPost::getStatus, 0);
        return this.update(updateWrapper);
    }

    private void validatePostPayload(CommunityPost post) {
        if (post == null) {
            throw new BusinessException("帖子内容不能为空");
        }
        String title = post.getTitle() == null ? "" : post.getTitle().trim();
        String content = post.getContent() == null ? "" : post.getContent().trim();
        if (title.length() < 2 || title.length() > 50) {
            throw new BusinessException("标题长度应为2-50个字符");
        }
        if (content.length() < 5) {
            throw new BusinessException("内容至少5个字");
        }
        Integer category = post.getCategory();
        if (category == null) {
            category = post.getType();
        }
        if (category == null || category < 1 || category > 5) {
            throw new BusinessException("帖子分类无效");
        }
        if (StringUtils.hasText(post.getImageUrl()) && post.getImageUrl().length() > 255) {
            throw new BusinessException("图片地址过长");
        }
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
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
        map.put("status", post.getStatus());
        map.put("createTime", post.getCreateTime());
        map.put("updateTime", post.getUpdateTime());

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
