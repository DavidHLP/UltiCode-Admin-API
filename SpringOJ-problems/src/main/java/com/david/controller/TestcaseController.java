package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.Testcase;
import com.david.service.TestcaseService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/testcases")
public class TestcaseController {

    @Resource
    private TestcaseService testcaseService;

    @GetMapping
    public ResponseResult<Page<Testcase>> getByPage(Page<Testcase> page) {
        return ResponseResult.success("", testcaseService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<Testcase> getById(@PathVariable Long id) {
        Testcase tc = testcaseService.getById(id);
        if (tc == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试用例不存在");
        }
        return ResponseResult.success("", tc);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody Testcase testcase) {
        boolean result = testcaseService.save(testcase);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody Testcase testcase) {
        Testcase exist = testcaseService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试用例不存在");
        }
        testcase.setId(id);
        boolean result = testcaseService.updateById(testcase);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        Testcase exist = testcaseService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试用例不存在");
        }
        boolean result = testcaseService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
