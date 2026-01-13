package com.syh.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syh.blog.entity.Category;

import java.util.List;

/**
 * 分类Service
 *
 * @author sunyahui
 * @since 2024-01-01
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类列表及文章数量
     */
    List<Category> listWithCount();
}
