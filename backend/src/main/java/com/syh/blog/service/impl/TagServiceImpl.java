package com.syh.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syh.blog.entity.Tag;
import com.syh.blog.mapper.TagMapper;
import com.syh.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.Duration;

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

    @Override
    public List<Tag> listWithCount() {
        // 尝试从缓存获取
        List<Tag> cachedTags = (List<Tag>) redisTemplate.opsForValue().get(TAG_CACHE_KEY);
        if (cachedTags != null) {
            return cachedTags;
        }

        // 从数据库查询
        List<Tag> tags = baseMapper.listWithCount();

        // 缓存结果（10分钟）
        redisTemplate.opsForValue().set(TAG_CACHE_KEY, tags, Duration.ofMinutes(10));

        return tags;
    }
}
