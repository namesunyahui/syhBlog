package com.syh.blog.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 归档响应VO
 *
 * @author sunyahui
 * @since 2025-01-19
 */
@Data
public class ArchiveVO {
    /**
     * 归档列表（按年分组）
     */
    private List<ArchiveDTO> archives;

    /**
     * 总文章数
     */
    private Integer totalCount;

    /**
     * 统计信息（年份数量、月份数量等）
     */
    private Map<String, Integer> statistics;
}
