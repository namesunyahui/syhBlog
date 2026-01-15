package com.syh.blog.controller;

import com.syh.blog.common.Result;
import com.syh.blog.entity.Tag;
import com.syh.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签Controller
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理")
@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @Operation(summary = "获取标签列表")
    @GetMapping
    public Result<List<Tag>> list() {
        List<Tag> tags = tagService.listWithCount();
        return Result.success(tags);
    }

    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public Result<Tag> detail(@PathVariable Long id) {
        Tag tag = tagService.getById(id);
        if (tag == null) {
            return Result.error("标签不存在");
        }
        return Result.success(tag);
    }

    // ==================== 管理后台接口 ====================

    @Operation(summary = "获取所有标签（管理后台）")
    @GetMapping("/all")
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.list();
        return Result.success(tags);
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Tag> createTag(@RequestBody Tag tag) {
        Tag saved = tagService.createTag(tag);
        return Result.success(saved);
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        Tag updated = tagService.updateTag(tag);
        return Result.success(updated);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
