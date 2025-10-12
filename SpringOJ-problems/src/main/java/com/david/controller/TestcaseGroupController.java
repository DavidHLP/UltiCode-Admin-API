package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.TestcaseGroup;
import com.david.service.TestcaseGroupService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/testcase-groups")
public class TestcaseGroupController {

    @Resource
    private TestcaseGroupService testcaseGroupService;

    @GetMapping
    public ResponseResult<Page<TestcaseGroup>> getByPage(Page<TestcaseGroup> page) {
        return ResponseResult.success("", testcaseGroupService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<TestcaseGroup> getById(@PathVariable Long id) {
        TestcaseGroup tg = testcaseGroupService.getById(id);
        if (tg == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试用例分组不存在");
        }
        return ResponseResult.success("", tg);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody TestcaseGroup testcaseGroup) {
        boolean result = testcaseGroupService.save(testcaseGroup);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody TestcaseGroup testcaseGroup) {
        TestcaseGroup exist = testcaseGroupService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试用例分组不存在");
        }
        testcaseGroup.setId(id);
        boolean result = testcaseGroupService.updateById(testcaseGroup);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        TestcaseGroup exist = testcaseGroupService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "测试用例分组不存在");
        }
        boolean result = testcaseGroupService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
