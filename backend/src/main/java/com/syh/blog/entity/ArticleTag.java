package com.syh.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章标签关联实体
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Data
@TableName("article_tags")
public class ArticleTag {

    /**
     * 文章ID
     */
    @TableId
    private Long articleId;

    /**
     * 标签ID
     */
    private Long tagId;
}
