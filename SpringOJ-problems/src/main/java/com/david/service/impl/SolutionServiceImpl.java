package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.calendar.enums.TargetType;
import com.david.entity.user.User;
import com.david.interfaces.UserServiceFeignClient;
import com.david.mapper.SolutionMapper;
import com.david.service.ILikeDislikeRecordService;
import com.david.service.ISolutionCommentService;
import com.david.service.ISolutionService;
import com.david.service.IUserContentViewService;
import com.david.solution.Solution;
import com.david.solution.UpDownCounts;
import com.david.solution.enums.ContentType;
import com.david.solution.vo.SolutionCardVo;
import com.david.solution.vo.SolutionDetailVo;
import com.david.usercontent.UserContentView;
import com.david.utils.MarkdownUtils;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    private final IUserContentViewService userContentViewService;
    private final ILikeDislikeRecordService likeDislikeRecordService;

    @Override
    @Transactional
    public SolutionDetailVo getSolutionDetailVoBy(Long solutionId, Long userId) {
        if (!userContentViewService.userHasViewedContent(userId, solutionId)) {
            userContentViewService.save(
                    UserContentView.builder()
                            .userId(userId)
                            .contentId(solutionId)
                            .contentType(ContentType.SOLUTION)
                            .build());
            solutionMapper.updateViews(solutionId);
        }
        Solution solution = solutionMapper.selectById(solutionId);
        User user = userServiceFeignClient.getUserById(solution.getUserId()).getData();
        return SolutionDetailVo.builder()
                .id(solution.getId())
                .problemId(solution.getProblemId())
                .userId(solution.getUserId())
                .content(solution.getContent())
                .title(solution.getTitle())
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

        // 1. 查询分页数据
        Page<SolutionCardVo> pageSolutions =
                solutionMapper.pageSolutionsCardVos(page, problemId, keyword);
        return fillInMissingContent(pageSolutions);
    }

    @Override
    public Page<SolutionCardVo> pageSolutionCardVosByUserId(
            Page<SolutionCardVo> page, Long userId) {
        Page<SolutionCardVo> pageSolutions =
                solutionMapper.pageSolutionCardVosByUserId(page, userId);
        return fillInMissingContent(pageSolutions);
    }

    private Page<SolutionCardVo> fillInMissingContent(Page<SolutionCardVo> pageSolutions) {
        List<SolutionCardVo> records = pageSolutions.getRecords();

        // 2. 处理空集合情况
        if (records == null || records.isEmpty()) {
            return pageSolutions;
        }

        // 3. 提取ID列表（一次操作，多次使用）
        List<Long> solutionIds = records.stream().map(SolutionCardVo::getId).toList();

        List<Long> userIds = records.stream().map(SolutionCardVo::getUserId).distinct().toList();

        // 4. 批量查询用户信息并转换为Map（增加空安全处理）
        Map<Long, User> userMap =
                Optional.ofNullable(userServiceFeignClient.getUserByIds(userIds))
                        .map(ResponseResult::getData)
                        .orElse(Collections.emptyList())
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        User::getUserId,
                                        Function.identity(),
                                        (existing, replacement) -> existing // 处理可能的重复用户ID
                                        ));

        // 5. 批量查询点赞点踩统计并转换为Map
        Map<Long, UpDownCounts> upDownCountsMap =
                likeDislikeRecordService
                        .getUpDownCountsByTargetTypeAndTargetIds(TargetType.SOLUTION, solutionIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        UpDownCounts::getTargetId,
                                        Function.identity(),
                                        (existing, replacement) -> existing));

        // 6. 批量处理VO对象（使用Optional避免空指针）
        records.forEach(
                solutionCardVo -> {
                    // 处理用户信息（空安全）
                    User user = userMap.getOrDefault(solutionCardVo.getUserId(), new User());
                    solutionCardVo.setAuthorUsername(user.getUsername());
                    solutionCardVo.setAuthorAvatar(user.getAvatar());

                    // 处理内容转换
                    solutionCardVo.setContentView(
                            MarkdownUtils.toPlainText(solutionCardVo.getContentView()));

                    // 处理点赞点踩数据（空安全）
                    UpDownCounts counts =
                            upDownCountsMap.getOrDefault(
                                    solutionCardVo.getId(),
                                    UpDownCounts.builder().upvotes(0).downvotes(0).build());
                    solutionCardVo.setUpvotes(counts.getUpvotes());
                    solutionCardVo.setDownvotes(counts.getDownvotes());
                });
        return pageSolutions;
    }
}
