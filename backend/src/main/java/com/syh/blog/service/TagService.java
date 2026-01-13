package com.syh.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syh.blog.entity.Tag;

import java.util.List;

/**
 * 标签Service
 *
 * @author sunyahui
 * @since 2024-01-01
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取标签列表及文章数量
     */
    List<Tag> listWithCount();
}
