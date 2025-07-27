package com.david.controller;

import com.david.dto.ProblemDto;
import com.david.dto.TestCaseDto;
import com.david.service.IProblemService;
import com.david.service.ITestCaseService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 题目前端控制器
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@RestController
@RequestMapping("/problems/api/view")
@RequiredArgsConstructor
public class ProblemViewController {

  private final IProblemService problemService;
  private final ITestCaseService testCaseService;

  @GetMapping("/{id}")
  public ResponseResult<ProblemDto> getProblemById(@PathVariable Long id) {
    ProblemDto problemDto = problemService.getProblemDtoById(id);
    if (problemDto == null) {
      return ResponseResult.fail(404, "题目不存在或已被删除");
    }
    List<TestCaseDto> testCaseDtos = testCaseService.getTestCaseDtoById(id);
    if (testCaseDtos == null){
      return ResponseResult.fail(404, "测试用例样例不存在");
    }
    problemDto.setTestCases(testCaseDtos);
    return ResponseResult.success("成功获取题目", problemDto);
  }
}
