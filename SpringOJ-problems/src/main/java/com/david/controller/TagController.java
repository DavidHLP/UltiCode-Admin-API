package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.Tag;
import com.david.service.TagService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Resource private TagService tagService;

    @GetMapping("/list")
    public ResponseResult<List<Tag>> getAllTags() {
        return ResponseResult.success("", tagService.list());
    }

    @GetMapping("/page")
    public ResponseResult<Page<Tag>> getByPage(Page<Tag> page) {
        return ResponseResult.success("", tagService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<Tag> getById(@PathVariable Long id) {
        Tag tag = tagService.getById(id);
        if (tag == null) {
            return ResponseResult.fail(ResponseCode.RC404, "标签不存在");
        }
        return ResponseResult.success("", tag);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody Tag tag) {
        boolean result = tagService.save(tag);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody Tag tag) {
        Tag exist = tagService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "标签不存在");
        }
        tag.setId(id);
        boolean result = tagService.updateById(tag);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        Tag exist = tagService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "标签不存在");
        }
        boolean result = tagService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
