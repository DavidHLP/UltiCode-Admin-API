package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.service.ISolutionService;
import com.david.solution.Solution;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;
import com.david.vo.SolutionCardVo;
import com.david.vo.SolutionDetailVo;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/solution")
public class SolutionViewController extends BaseController {

    private final ISolutionService solutionService;

    @PostMapping
    public ResponseResult<Void> createSolution(@RequestBody Solution solution) {
		solution.setUserId(getCurrentUserId());
        if (solutionService.save(solution)) {
            return ResponseResult.success("题解创建成功");
        }
        return ResponseResult.fail(500, "题解创建失败");
    }

    @PutMapping
    public ResponseResult<Void> updateSolution(@RequestBody Solution solution) {
        if (solutionService.updateById(solution)) {
            return ResponseResult.success("题解更新成功");
        }
        return ResponseResult.fail(500, "题解更新失败");
    }

    @DeleteMapping
    public ResponseResult<Void> deleteSolution(@RequestBody Solution solution) {
        if (solutionService.removeById(solution)) {
            return ResponseResult.success("题解删除成功");
        }
        return ResponseResult.fail(500, "题解删除失败");
    }

    @GetMapping
    public ResponseResult<SolutionDetailVo> getSolution(@RequestParam Long solutionId) {
        SolutionDetailVo solutionDetailVo = solutionService.getSolutionDetailVoBy(solutionId);
        if (solutionDetailVo == null) {
            return ResponseResult.fail(404, "题解不存在");
        }
        return ResponseResult.success("获取题解成功", solutionDetailVo);
    }

    @GetMapping("/page")
    public ResponseResult<Page<SolutionCardVo>> pageSolutionCardVos(
            @RequestParam long page,
            @RequestParam long size,
            @RequestParam Long problemId,
            @RequestParam(required = false) String keyword) {
        Page<SolutionCardVo> p = new Page<>(page, size);
        Page<SolutionCardVo> result = solutionService.pageSolutionCardVos(p, problemId, keyword);
        return ResponseResult.success("获取题解成功", result);
    }
}
