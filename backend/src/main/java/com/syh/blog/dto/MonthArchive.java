package com.syh.blog.dto;

import com.syh.blog.entity.Article;
import lombok.Data;
import java.util.List;

/**
 * 月归档DTO
 *
 * @author sunyahui
 * @since 2025-01-19
 */
@Data
public class MonthArchive {
    /**
     * 月份 (1-12)
     */
    private Integer month;

    /**
     * 该月文章数量
     */
    private Integer count;

    /**
     * 文章列表
     */
    private List<Article> articles;
}
