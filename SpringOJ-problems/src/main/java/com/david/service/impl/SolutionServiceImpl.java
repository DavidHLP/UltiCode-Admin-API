package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.user.User;
import com.david.interfaces.UserServiceFeignClient;
import com.david.mapper.SolutionMapper;
import com.david.service.ISolutionCommentService;
import com.david.service.ISolutionService;
import com.david.solution.Solution;
import com.david.solution.vo.SolutionCardVo;
import com.david.solution.vo.SolutionDetailVo;
import com.david.utils.MarkdownUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题解服务实现类
 *
 * @author david
 * @since 2025-07-28
 */
@Service
@RequiredArgsConstructor
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper, Solution>
        implements ISolutionService {
    private final SolutionMapper solutionMapper;
    private final ISolutionCommentService solutionCommentService;
    private final UserServiceFeignClient userServiceFeignClient;

    @Override
    public SolutionDetailVo getSolutionDetailVoBy(Long solutionId) {
        Solution solution = solutionMapper.selectById(solutionId);
        User user = userServiceFeignClient.getUserById(solution.getUserId()).getData();
        return SolutionDetailVo.builder()
                .id(solution.getId())
                .problemId(solution.getProblemId())
                .userId(solution.getUserId())
                .content(solution.getContent())
                .title(solution.getTitle())
                .upvotes(solution.getUpvotes())
                .downvotes(solution.getDownvotes())
                .comments(solution.getComments())
                .views(solution.getViews())
                .authorUsername(user.getUsername())
                .authorAvatar(user.getAvatar())
                .solutionComments(solutionCommentService.getSolutionCommentVos(solution.getId()))
                .language(solution.getLanguage())
                .build();
    }

    @Override
    public Page<SolutionCardVo> pageSolutionCardVos(
            Page<SolutionCardVo> page, Long problemId, String keyword) {
        Page<SolutionCardVo> pageSolutions =
                solutionMapper.pageSolutionsCardVos(page, problemId, keyword);
        if (pageSolutions.getRecords() == null || pageSolutions.getRecords().isEmpty()) {
            return pageSolutions;
        }
        Map<Long, User> userMap =
                userServiceFeignClient
                        .getUserByIds(
                                pageSolutions.getRecords().stream()
                                        .map(SolutionCardVo::getUserId)
                                        .distinct()
                                        .toList())
                        .getData()
                        .stream()
                        .collect(Collectors.toMap(User::getUserId, user -> user));
        pageSolutions
                .getRecords()
                .forEach(
                        solutionCardVo -> {
                            solutionCardVo.setAuthorUsername(
                                    userMap.get(solutionCardVo.getUserId()).getUsername());
                            solutionCardVo.setAuthorAvatar(
                                    userMap.get(solutionCardVo.getUserId()).getAvatar());
                            solutionCardVo.setContentView(
                                    MarkdownUtils.toPlainText(solutionCardVo.getContentView()));
                        });
        return pageSolutions;
    }
}
