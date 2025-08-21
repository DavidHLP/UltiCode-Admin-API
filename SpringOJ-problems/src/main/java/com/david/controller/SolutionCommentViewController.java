package com.david.controller;

import com.david.service.ISolutionCommentService;
import com.david.solution.SolutionComments;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;
import com.david.exception.BizException;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/solution/comment")
public class SolutionCommentViewController extends BaseController {
    private final ISolutionCommentService solutionCommentService;

    @PostMapping
    public ResponseResult<Void> createComment(@RequestBody @Validated(SolutionComments.Create.class) SolutionComments solutionComments) {
        solutionComments.setUserId(getCurrentUserId());
        if (!solutionCommentService.save(solutionComments)) {
            throw new BizException("创建评论失败");
        }
        return ResponseResult.success("成功创建评论");
    }

    @PutMapping
    public ResponseResult<Void> updateComment(@RequestBody @Validated(SolutionComments.Update.class) SolutionComments solutionComments) {
        if (!solutionCommentService.updateById(solutionComments)) {
            throw new BizException("更新评论失败");
        }
        return ResponseResult.success("成功更新评论");
    }

    @DeleteMapping
    public ResponseResult<Void> deleteComment(@RequestBody @Validated(SolutionComments.Delete.class) SolutionComments solutionComments) {
        if (!solutionCommentService.removeById(solutionComments)) {
            throw new BizException("删除评论失败");
        }
        return ResponseResult.success("成功删除评论");
    }
}

