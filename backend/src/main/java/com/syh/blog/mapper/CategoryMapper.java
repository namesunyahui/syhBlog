package com.syh.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syh.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类Mapper
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 获取分类及其文章数量
     */
    @Select("SELECT c.*, COUNT(a.id) as article_count " +
            "FROM categories c " +
            "LEFT JOIN articles a ON c.id = a.category_id AND a.is_published = true " +
            "GROUP BY c.id " +
            "ORDER BY c.sort_order ASC")
    List<Category> listWithCount();
}
