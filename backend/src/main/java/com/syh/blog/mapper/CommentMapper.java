package com.syh.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syh.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
