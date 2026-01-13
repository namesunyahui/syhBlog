package com.syh.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syh.blog.entity.Comment;
import com.syh.blog.mapper.CommentMapper;
import com.syh.blog.service.CommentService;
import org.springframework.stereotype.Service;

/**
 * 评论Service实现
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
