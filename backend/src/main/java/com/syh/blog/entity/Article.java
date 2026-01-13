package com.syh.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@TableName("articles")
public class Article {

    /**
     * 文章ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容（Markdown格式）
     */
    private String content;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图片URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 所属分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Long viewCount;

    /**
     * 是否已发布
     */
    @TableField("is_published")
    private Boolean isPublished;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 分类信息（非数据库字段）
     */
    @TableField(exist = false)
    private Category category;

    /**
     * 标签列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Tag> tags;

    /**
     * 作者信息（非数据库字段）
     */
    @TableField(exist = false)
    private User author;
}
