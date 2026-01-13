package com.syh.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syh.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签Mapper
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 获取标签及其文章数量
     */
    @Select("SELECT t.*, COUNT(at.article_id) as article_count " +
            "FROM tags t " +
            "LEFT JOIN article_tags at ON t.id = at.tag_id " +
            "LEFT JOIN articles a ON at.article_id = a.id AND a.is_published = true " +
            "GROUP BY t.id " +
            "ORDER BY article_count DESC")
    List<Tag> listWithCount();
}
