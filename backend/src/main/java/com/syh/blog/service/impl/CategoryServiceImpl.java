package com.syh.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syh.blog.entity.Category;
import com.syh.blog.mapper.CategoryMapper;
import com.syh.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.Duration;

/**
 * 分类Service实现
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private static final String CATEGORY_CACHE_KEY = "category:all";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Category> listWithCount() {
        // 尝试从缓存获取
        List<Category> cachedCategories = (List<Category>) redisTemplate.opsForValue().get(CATEGORY_CACHE_KEY);
        if (cachedCategories != null) {
            return cachedCategories;
        }

        // 从数据库查询
        List<Category> categories = baseMapper.listWithCount();

        // 缓存结果（10分钟）
        redisTemplate.opsForValue().set(CATEGORY_CACHE_KEY, categories, Duration.ofMinutes(10));

        return categories;
    }
}
