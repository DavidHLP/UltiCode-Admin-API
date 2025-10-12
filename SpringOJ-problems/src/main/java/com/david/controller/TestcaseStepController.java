package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.TestcaseStep;
import com.david.service.TestcaseStepService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/testcase-steps")
public class TestcaseStepController {

    @Resource
    private TestcaseStepService testcaseStepService;

    @GetMapping
    public ResponseResult<Page<TestcaseStep>> getByPage(Page<TestcaseStep> page) {
        return ResponseResult.success("", testcaseStepService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<TestcaseStep> getById(@PathVariable Long id) {
        TestcaseStep ts = testcaseStepService.getById(id);
        if (ts == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试步骤不存在");
        }
        return ResponseResult.success("", ts);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody TestcaseStep testcaseStep) {
        boolean result = testcaseStepService.save(testcaseStep);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody TestcaseStep testcaseStep) {
        TestcaseStep exist = testcaseStepService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试步骤不存在");
        }
        testcaseStep.setId(id);
        boolean result = testcaseStepService.updateById(testcaseStep);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        TestcaseStep exist = testcaseStepService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试步骤不存在");
        }
        boolean result = testcaseStepService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
