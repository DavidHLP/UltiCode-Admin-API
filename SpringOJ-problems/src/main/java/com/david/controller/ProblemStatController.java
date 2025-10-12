package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.ProblemStat;
import com.david.service.ProblemStatService;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/problem-stats")
public class ProblemStatController {

    @Resource
    private ProblemStatService problemStatService;

    @GetMapping
    public ResponseResult<Page<ProblemStat>> getByPage(Page<ProblemStat> page) {
        return ResponseResult.success("", problemStatService.page(page));
    }

    @GetMapping("/{problemId}")
    public ResponseResult<ProblemStat> getById(@PathVariable Long problemId) {
        ProblemStat stat = problemStatService.getById(problemId);
        if (stat == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目统计不存在");
        }
        return ResponseResult.success("", stat);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody ProblemStat problemStat) {
        boolean result = problemStatService.save(problemStat);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{problemId}")
    public ResponseResult<Boolean> update(@PathVariable Long problemId, @RequestBody ProblemStat problemStat) {
        ProblemStat exist = problemStatService.getById(problemId);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目统计不存在");
        }
        problemStat.setProblemId(problemId);
        boolean result = problemStatService.updateById(problemStat);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{problemId}")
    public ResponseResult<Boolean> delete(@PathVariable Long problemId) {
        ProblemStat exist = problemStatService.getById(problemId);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目统计不存在");
        }
        boolean result = problemStatService.removeById(problemId);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }
}
