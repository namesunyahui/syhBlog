package com.syh.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syh.blog.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章标签关联Mapper
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
}
