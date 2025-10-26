package com.david.judge.controller;

import com.david.core.http.ApiResponse;
import com.david.judge.dto.JudgeNodeView;
import com.david.judge.service.JudgeNodeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('platform_admin')")
@RequestMapping("/api/judge/nodes")
public class JudgeNodeController {

    private final JudgeNodeService judgeNodeService;

    @GetMapping
    public ApiResponse<List<JudgeNodeView>> listNodes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        List<JudgeNodeView> nodes = judgeNodeService.listNodes(status, keyword);
        return ApiResponse.success(nodes);
    }
}
