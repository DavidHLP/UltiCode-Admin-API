package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.ProblemLanguageConfig;
import com.david.service.ProblemLanguageConfigService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/problem-language-configs")
public class ProblemLanguageConfigController {

    @Resource
    private ProblemLanguageConfigService problemLanguageConfigService;

    @GetMapping
    public ResponseResult<Page<ProblemLanguageConfig>> getByPage(Page<ProblemLanguageConfig> page) {
        return ResponseResult.success("", problemLanguageConfigService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<ProblemLanguageConfig> getById(@PathVariable Long id) {
        ProblemLanguageConfig cfg = problemLanguageConfigService.getById(id);
        if (cfg == null) {
            return ResponseResult.fail(ResponseCode.RC404, "配置不存在");
        }
        return ResponseResult.success("", cfg);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody ProblemLanguageConfig problemLanguageConfig) {
        boolean result = problemLanguageConfigService.save(problemLanguageConfig);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody ProblemLanguageConfig problemLanguageConfig) {
        ProblemLanguageConfig exist = problemLanguageConfigService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "配置不存在");
        }
        problemLanguageConfig.setId(id);
        boolean result = problemLanguageConfigService.updateById(problemLanguageConfig);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        ProblemLanguageConfig exist = problemLanguageConfigService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "配置不存在");
        }
        boolean result = problemLanguageConfigService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
