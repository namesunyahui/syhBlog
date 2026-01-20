package com.syh.blog.dto;

import com.syh.blog.entity.Article;
import lombok.Data;
import java.util.List;

/**
 * 年归档DTO
 *
 * @author sunyahui
 * @since 2025-01-19
 */
@Data
public class ArchiveDTO {
    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份列表
     */
    private List<MonthArchive> months;
}
