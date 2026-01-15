package com.syh.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syh.blog.entity.Tag;
import com.syh.blog.mapper.TagMapper;
import com.syh.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Duration;
import java.util.Set;

/**
 * 标签Service实现
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private static final String TAG_CACHE_KEY = "tag:all";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查Redis是否可用
     */
    private boolean isRedisAvailable() {
        return redisTemplate != null;
    }

    @Override
    public List<Tag> listWithCount() {
        // 尝试从缓存获取
        if (isRedisAvailable()) {
            try {
                List<Tag> cachedTags = (List<Tag>) redisTemplate.opsForValue().get(TAG_CACHE_KEY);
                if (cachedTags != null) {
                    return cachedTags;
                }
            } catch (Exception e) {
                System.err.println("Redis缓存读取失败，降级到数据库查询: " + e.getMessage());
            }
        }

        // 从数据库查询
        List<Tag> tags = baseMapper.listWithCount();

        // 缓存结果（10分钟）
        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().set(TAG_CACHE_KEY, tags, Duration.ofMinutes(10));
            } catch (Exception e) {
                System.err.println("Redis缓存写入失败: " + e.getMessage());
            }
        }

        return tags;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tag createTag(Tag tag) {
        // 检查标签名是否已存在
        long count = lambdaQuery()
                .eq(Tag::getName, tag.getName())
                .count();
        if (count > 0) {
            throw new RuntimeException("标签名已存在");
        }

        save(tag);
        clearCache();
        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tag updateTag(Tag tag) {
        Tag existing = getById(tag.getId());
        if (existing == null) {
            throw new RuntimeException("标签不存在");
        }

        // 检查标签名是否与其他标签重复
        long count = lambdaQuery()
                .eq(Tag::getName, tag.getName())
                .ne(Tag::getId, tag.getId())
                .count();
        if (count > 0) {
            throw new RuntimeException("标签名已存在");
        }

        updateById(tag);
        clearCache();
        return getById(tag.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTag(Long id) {
        boolean deleted = removeById(id);
        if (deleted) {
            clearCache();
        }
        return deleted;
    }

    /**
     * 清除标签缓存
     */
    private void clearCache() {
        if (isRedisAvailable()) {
            try {
                redisTemplate.delete(TAG_CACHE_KEY);
            } catch (Exception e) {
                System.err.println("清除缓存失败: " + e.getMessage());
            }
        }
    }
}
