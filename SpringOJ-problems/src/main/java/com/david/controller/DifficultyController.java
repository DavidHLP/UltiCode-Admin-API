package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.Difficulty;
import com.david.service.DifficultyService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/difficulties")
public class DifficultyController {

    @Resource
    private DifficultyService difficultyService;

    @GetMapping
    public ResponseResult<Page<Difficulty>> getByPage(Page<Difficulty> page) {
        return ResponseResult.success("", difficultyService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<Difficulty> getById(@PathVariable Integer id) {
        Difficulty d = difficultyService.getById(id);
        if (d == null) {
            return ResponseResult.fail(ResponseCode.RC404, "难度不存在");
        }
        return ResponseResult.success("", d);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody Difficulty difficulty) {
        boolean result = difficultyService.save(difficulty);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Integer id, @RequestBody Difficulty difficulty) {
        Difficulty exist = difficultyService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "难度不存在");
        }
        difficulty.setId(id);
        boolean result = difficultyService.updateById(difficulty);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Integer id) {
        Difficulty exist = difficultyService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "难度不存在");
        }
        boolean result = difficultyService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
