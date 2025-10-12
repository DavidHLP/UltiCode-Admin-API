package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.AdminProblemDetailDto;
import com.david.entity.*;
import com.david.service.*;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Resource private ProblemService problemService;
    @Resource private ProblemLocaleService problemLocaleService;
    @Resource private ProblemLanguageConfigService problemLanguageConfigService;
    @Resource private ProblemStatService problemStatService;
    @Resource private TagService tagService;
    @Resource private ProblemTagService problemTagService;
    @Resource private TestcaseGroupService testcaseGroupService;
    @Resource private TestcaseService testcaseService;
    @Resource private TestcaseStepService testcaseStepService;

    @GetMapping
    public ResponseResult<Page<Problem>> getByPage(Page<Problem> page) {
        return ResponseResult.success("", problemService.page(page));
    }

    @GetMapping("/{id}")
    public ResponseResult<Problem> getById(@PathVariable Long id) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        return ResponseResult.success("", problem);
    }

    @PostMapping
    public ResponseResult<Boolean> save(@RequestBody Problem problem) {
        boolean result = problemService.save(problem);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "保存失败");
        }
        return ResponseResult.success("保存成功", true);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody Problem problem) {
        Problem exist = problemService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        problem.setId(id);
        boolean result = problemService.updateById(problem);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "更新失败");
        }
        return ResponseResult.success("更新成功", true);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        Problem exist = problemService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        boolean result = problemService.removeById(id);
        if (!result) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "删除失败");
        }
        return ResponseResult.success("删除成功", true);
    }

    @GetMapping("/{id}/admin-detail")
    public ResponseResult<AdminProblemDetailDto> getAdminDetail(@PathVariable Long id) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }

        AdminProblemDetailDto dto = new AdminProblemDetailDto();
        dto.setProblem(problem);
        dto.setLocales(problemLocaleService.lambdaQuery().eq(ProblemLocale::getProblemId, id).list());
        dto.setLanguageConfigs(problemLanguageConfigService.lambdaQuery().eq(ProblemLanguageConfig::getProblemId, id).list());
        dto.setStat(problemStatService.getById(id));

        List<Tag> all = tagService.list();
        dto.setAllTags(all);
        List<Long> selected = problemTagService.lambdaQuery().eq(ProblemTag::getProblemId, id).list()
                .stream().map(ProblemTag::getTagId).collect(Collectors.toList());
        dto.setSelectedTagIds(selected);

        List<TestcaseGroup> groups = testcaseGroupService.lambdaQuery().eq(TestcaseGroup::getProblemId, id).list();
        List<AdminProblemDetailDto.TestcaseGroupDto> groupDtos = new ArrayList<>();
        for (TestcaseGroup g : groups) {
            AdminProblemDetailDto.TestcaseGroupDto gDto = new AdminProblemDetailDto.TestcaseGroupDto();
            gDto.setGroup(g);
            List<Testcase> tcs = testcaseService.lambdaQuery().eq(Testcase::getGroupId, g.getId()).list();
            List<AdminProblemDetailDto.TestcaseDto> tDtos = new ArrayList<>();
            for (Testcase t : tcs) {
                AdminProblemDetailDto.TestcaseDto tDto = new AdminProblemDetailDto.TestcaseDto();
                tDto.setTestcase(t);
                List<TestcaseStep> steps = testcaseStepService.lambdaQuery().eq(TestcaseStep::getTestcaseId, t.getId()).list();
                tDto.setSteps(steps);
                tDtos.add(tDto);
            }
            gDto.setTestcases(tDtos);
            groupDtos.add(gDto);
        }
        dto.setGroups(groupDtos);

        return ResponseResult.success("", dto);
    }

    @GetMapping("/{id}/tags")
    public ResponseResult<List<Long>> getProblemTags(@PathVariable Long id) {
        Problem exist = problemService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        List<Long> tagIds = problemTagService.lambdaQuery().eq(ProblemTag::getProblemId, id).list()
                .stream().map(ProblemTag::getTagId).collect(Collectors.toList());
        return ResponseResult.success("", tagIds);
    }

    @PutMapping("/{id}/tags")
    public ResponseResult<Boolean> updateProblemTags(@PathVariable Long id, @RequestBody List<Long> tagIds) {
        Problem exist = problemService.getById(id);
        if (exist == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        problemTagService.lambdaUpdate().eq(ProblemTag::getProblemId, id).remove();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<ProblemTag> links = tagIds.stream().map(tid -> {
                ProblemTag pt = new ProblemTag();
                pt.setProblemId(id);
                pt.setTagId(tid);
                return pt;
            }).collect(Collectors.toList());
            boolean saved = problemTagService.saveBatch(links);
            if (!saved) {
                return ResponseResult.fail(ResponseCode.BUSINESS_ERROR, "标签更新失败");
            }
        }
        return ResponseResult.success("更新成功", true);
    }

    @GetMapping("/{id}/locales")
    public ResponseResult<List<ProblemLocale>> listLocalesByProblem(@PathVariable Long id) {
        if (problemService.getById(id) == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        return ResponseResult.success("", problemLocaleService.lambdaQuery().eq(ProblemLocale::getProblemId, id).list());
    }

    @GetMapping("/{id}/language-configs")
    public ResponseResult<List<ProblemLanguageConfig>> listLangConfigsByProblem(@PathVariable Long id) {
        if (problemService.getById(id) == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        return ResponseResult.success("", problemLanguageConfigService.lambdaQuery().eq(ProblemLanguageConfig::getProblemId, id).list());
    }

    @GetMapping("/{id}/testcase-groups")
    public ResponseResult<List<TestcaseGroup>> listGroupsByProblem(@PathVariable Long id) {
        if (problemService.getById(id) == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        return ResponseResult.success("", testcaseGroupService.lambdaQuery().eq(TestcaseGroup::getProblemId, id).list());
    }

    @GetMapping("/{id}/stats")
    public ResponseResult<ProblemStat> getStat(@PathVariable Long id) {
        if (problemService.getById(id) == null) {
            return ResponseResult.fail(ResponseCode.RC404, "题目不存在");
        }
        return ResponseResult.success("", problemStatService.getById(id));
    }
}
