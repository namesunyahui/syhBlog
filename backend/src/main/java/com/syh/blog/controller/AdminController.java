package com.syh.blog.controller;

import com.syh.blog.service.ArticleService;
import com.syh.blog.service.CommentService;
import com.syh.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台Controller
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Tag(name = "管理后台")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public com.syh.blog.common.Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 文章总数
        long articleCount = articleService.count();
        stats.put("articleCount", articleCount);

        // 总浏览量
        Long viewCount = articleService.getTotalViewCount();
        stats.put("viewCount", viewCount != null ? viewCount : 0);

        // 评论总数
        long commentCount = commentService.count();
        stats.put("commentCount", commentCount);

        // 分类数量
        long categoryCount = categoryService.count();
        stats.put("categoryCount", categoryCount);

        return com.syh.blog.common.Result.success(stats);
    }
}
