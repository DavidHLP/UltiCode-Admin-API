package com.david.controller;

import com.david.service.ISolutionService;
import com.david.solution.Solution;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/solution")
public class SolutionViewController extends BaseController {

	private final ISolutionService solutionService;
	@PostMapping
	public ResponseResult<Solution> createSolution(@RequestBody Solution solution){
		if(solutionService.save(solution)){
			return ResponseResult.success("题解创建成功");
		}
		return ResponseResult.fail(500,"题解创建失败");
	}
}
