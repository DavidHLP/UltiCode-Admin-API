package com.david.controller;

import com.david.solution.Solution;
import com.david.service.ISolutionService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/solutions/api/management")
public class SolutionManagementController {

    private final ISolutionService solutionService;

    @GetMapping
    public ResponseResult<List<Solution>> getAllSolutions() {
        return ResponseResult.success("成功获取所有题解", solutionService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<Solution> getSolutionById(@PathVariable Long id) {
        Solution solution = solutionService.getById(id);
        if (solution == null) {
            return ResponseResult.fail(404, "题解不存在");
        }
        return ResponseResult.success("成功获取题解", solution);
    }

    @PostMapping
    public ResponseResult<Void> createSolution(@RequestBody Solution solution) {
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

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteSolution(@PathVariable Long id) {
        if (solutionService.removeById(id)) {
            return ResponseResult.success("题解删除成功");
        }
        return ResponseResult.fail(500, "题解删除失败");
    }
}
