package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.Category;
import com.david.service.CategoryService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping
    public ResponseResult<Page<Category>> getByPage(Page<Category> page) {
        return ResponseResult.success("", categoryService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<Category> getById(@PathVariable Integer id) {
        Category c = categoryService.getById(id);
        if (c == null) {
            return ResponseResult.fail(ResponseCode.RC404, "分类不存在");
        }
        return ResponseResult.success("", c);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody Category category) {
        boolean result = categoryService.save(category);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Integer id, @RequestBody Category category) {
        Category exist = categoryService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "分类不存在");
        }
        category.setId(id);
        boolean result = categoryService.updateById(category);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Integer id) {
        Category exist = categoryService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "分类不存在");
        }
        boolean result = categoryService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
