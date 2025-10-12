package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.ProblemLocale;
import com.david.service.ProblemLocaleService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/problem-locales")
public class ProblemLocaleController {

    @Resource
    private ProblemLocaleService problemLocaleService;

    @GetMapping
    public ResponseResult<Page<ProblemLocale>> getByPage(Page<ProblemLocale> page) {
        return ResponseResult.success("", problemLocaleService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<ProblemLocale> getById(@PathVariable Long id) {
        ProblemLocale pl = problemLocaleService.getById(id);
        if (pl == null) {
            return ResponseResult.fail(ResponseCode.RC404, "多语言内容不存在");
        }
        return ResponseResult.success("", pl);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody ProblemLocale problemLocale) {
        boolean result = problemLocaleService.save(problemLocale);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody ProblemLocale problemLocale) {
        ProblemLocale exist = problemLocaleService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "多语言内容不存在");
        }
        problemLocale.setId(id);
        boolean result = problemLocaleService.updateById(problemLocale);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        ProblemLocale exist = problemLocaleService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "多语言内容不存在");
        }
        boolean result = problemLocaleService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
