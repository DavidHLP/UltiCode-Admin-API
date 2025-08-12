package com.david.controller;

import com.david.dto.SolutionCommentDto;
import com.david.service.ISolutionCommentService;
import com.david.solution.SolutionComments;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;
import com.david.vo.SolutionCommentVo;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/problems/api/solution-comments")
@RequiredArgsConstructor
public class SolutionCommentsController extends BaseController {

    private final ISolutionCommentService solutionCommentService;

    @PostMapping
    public ResponseResult<Void> createComment(@RequestBody SolutionCommentDto commentDto) {
        SolutionComments solutionComments = new SolutionComments();
        solutionComments.setSolutionId(commentDto.getSolutionId());
        solutionComments.setUserId(getCurrentUserId());
        solutionComments.setContent(commentDto.getContent());
        solutionComments.setParentId(commentDto.getParentId());
        solutionComments.setReplyToUserId(commentDto.getReplyToUserId());

        if (commentDto.getParentId() != null) {
            SolutionComments parentComment = solutionCommentService.getById(commentDto.getParentId());
            if (parentComment != null) {
                solutionComments.setRootId(parentComment.getRootId() == null ? parentComment.getId() : parentComment.getRootId());
            }
        }

        solutionCommentService.save(solutionComments);
        return ResponseResult.success("成功创建评论");
    }

    @GetMapping("/solution/{solutionId}")
    public ResponseResult<List<SolutionCommentVo>> getCommentsBySolutionId(@PathVariable Long solutionId) {
        return ResponseResult.success("成功获取题解评论列表", solutionCommentService.getCommentsBySolutionId(solutionId));
    }
}


