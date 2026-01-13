package com.syh.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syh.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文章Mapper
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 搜索文章
     */
    @Select("SELECT * FROM articles " +
            "WHERE status = 'PUBLISHED' " +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY created_at DESC")
    IPage<Article> searchArticles(Page<Article> page, @Param("keyword") String keyword);

    /**
     * 获取归档文章
     */
    @Select("SELECT * FROM articles " +
            "WHERE status = 'PUBLISHED' " +
            "ORDER BY created_at DESC")
    IPage<Article> getArchiveList(Page<Article> page);
}
