package com.david.controller;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.SolutionQueryDto;
import com.david.service.ISolutionService;
import com.david.solution.Solution;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;
import com.david.vo.SolutionCardVo;
import com.david.vo.SolutionVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/solutions/api/view")
public class SolutionViewController extends BaseController {

	private final ISolutionService solutionService;

	@GetMapping("/problem")
	public ResponseResult<Page<SolutionCardVo>> getSolutionsByProblemId(SolutionQueryDto solutionQueryDto) {
		// 创建分页对象
		Page<SolutionCardVo> page = new Page<>(solutionQueryDto.getPage(), solutionQueryDto.getSize());

		// 调用服务层方法进行分页查询
		Page<SolutionCardVo> result = solutionService.getSolutionsByProblemIdWithPage(page,
				solutionQueryDto.getProblemId(), solutionQueryDto.getTitle(), solutionQueryDto.getSort());
		return ResponseResult.success("成功获取题解信息", result);
	}

	@GetMapping("/")
	public ResponseResult<SolutionVo> getSolutionById(@RequestParam("id") Long id) {
		SolutionVo solution = solutionService.getSolutionById(id, getCurrentUserId());
		if (solution == null) {
			return ResponseResult.fail(404, "题解不存在");
		}
		return ResponseResult.success("成功获取题解信息", solution);
	}

	@PostMapping("/")
	public ResponseResult<Void> createSolution(@RequestBody Solution solution) {
		solution.setUserId(getCurrentUserId());
		if (solutionService.save(solution)) {
			return ResponseResult.success("提交记录创建成功");
		}
		return ResponseResult.fail(500, "提交记录创建失败");
	}

	@PutMapping("/")
	public ResponseResult<Void> updateSolution(@RequestBody Solution solution) {
		solution.setUserId(getCurrentUserId());
		if (solutionService.updateById(solution)) {
			return ResponseResult.success("提交记录更新成功");
		}
		return ResponseResult.fail(500, "提交记录更新失败");
	}

	@PostMapping("/vote/{solutionId}/{type}")
	public ResponseResult<Void> voteSolution(@PathVariable("solutionId") Long solutionId,
			@PathVariable("type") String type) {
		solutionService.voteSolution(solutionId, type);
		return ResponseResult.success("投票成功");
	}
}
