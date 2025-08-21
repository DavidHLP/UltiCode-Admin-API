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
import com.david.solution.enums.SolutionStatus;
import com.david.solution.vo.SolutionCardVo;
import com.david.solution.vo.SolutionDetailVo;
import com.david.solution.vo.SolutionManagementCardVo;
import com.david.usercontent.UserContentView;
import com.david.utils.MarkdownUtils;
import com.david.utils.ResponseResult;
import com.david.exception.BizException;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
@Validated
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper, Solution>
        implements ISolutionService {
    private final SolutionMapper solutionMapper;
    private final ISolutionCommentService solutionCommentService;
    private final UserServiceFeignClient userServiceFeignClient;
    private final IUserContentViewService userContentViewService;
    private final ILikeDislikeRecordService likeDislikeRecordService;

    @Override
    @Transactional
    public SolutionDetailVo getSolutionDetailVoBy(
            Long solutionId,
            Long userId) {
        if (userId != null && userId < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(),
                    "用户ID必须>=1，当前值：" + userId);
        }
        Solution solution = solutionMapper.selectApprovedById(solutionId);
        if (solution == null) {
            throw BizException.of(ResponseCode.RC404.getCode(), "题解不存在，ID：" + solutionId);
        }
        if (userId != null && userId >= 1) {
            if (!userContentViewService.userHasViewedContent(userId, solutionId)) {
                userContentViewService.save(
                        UserContentView.builder()
                                .userId(userId)
                                .contentId(solutionId)
                                .contentType(ContentType.SOLUTION)
                                .build());
                solutionMapper.updateViews(solutionId);
            }
        } else {
            // 匿名或无效用户ID：不记录用户浏览记录，仅增加浏览量
            solutionMapper.updateViews(solutionId);
        }
        User user = userServiceFeignClient.getUserById(solution.getUserId()).getData();
        return SolutionDetailVo.builder()
                .id(solution.getId())
                .problemId(solution.getProblemId())
                .userId(solution.getUserId())
                .content(solution.getContent())
                .title(solution.getTitle())
                .comments(solution.getComments())
                .views(solution.getViews())
                .authorUsername(user != null ? user.getUsername() : null)
                .authorAvatar(user != null ? user.getAvatar() : null)
                .solutionComments(solutionCommentService.getSolutionCommentVos(solution.getId()))
                .language(solution.getLanguage())
                .build();
    }

    @Override
    public Page<SolutionCardVo> pageSolutionCardVos(
            Page<SolutionCardVo> page,
            Long problemId,
            String keyword) {
        if (page.getCurrent() < 1 || page.getSize() < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(),
                    String.format("分页参数无效：current=%d, size=%d", page.getCurrent(), page.getSize()));
        }
        // 1. 查询分页数据
        Page<SolutionCardVo> pageSolutions =
                solutionMapper.pageSolutionsCardVos(page, problemId, keyword);
        return fillInMissingContent(pageSolutions);
    }

    @Override
    public Page<SolutionCardVo> pageSolutionCardVosByUserId(
            Page<SolutionCardVo> page,
            Long userId) {
        if (page.getCurrent() < 1 || page.getSize() < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(),
                    String.format("分页参数无效：current=%d, size=%d", page.getCurrent(), page.getSize()));
        }
        Page<SolutionCardVo> pageSolutions =
                solutionMapper.pageSolutionCardVosByUserId(page, userId);
        return fillInMissingContent(pageSolutions);
    }

    @Override
    public Page<SolutionManagementCardVo> pageSolutionManagementCardVo(
            Page<SolutionManagementCardVo> page,
            Long problemId,
            String keyword,
            Long userId,
            SolutionStatus status) {
        if (page.getCurrent() < 1 || page.getSize() < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(),
                    String.format("分页参数无效：current=%d, size=%d", page.getCurrent(), page.getSize()));
        }
        Page<SolutionManagementCardVo> pages =
                solutionMapper.pageSolutionManagementCardVos(page, problemId, keyword, userId, status);
        List<SolutionManagementCardVo> records = pages.getRecords();
        if (records == null || records.isEmpty()) {
            return pages;
        }
        List<Long> userIds = records.stream().map(SolutionManagementCardVo::getUserId).distinct().toList();
        Map<Long, User> userMap = loadUsersMapByIds(userIds);
        records.forEach(vo -> {
            User u = userMap.getOrDefault(vo.getUserId(), new User());
            vo.setAuthorUsername(u.getUsername());
            vo.setAuthorAvatar(u.getAvatar());
        });
        return pages;
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
        Map<Long, User> userMap = loadUsersMapByIds(userIds);

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

    private Map<Long, User> loadUsersMapByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return Optional.ofNullable(userServiceFeignClient.getUserByIds(userIds))
                .map(ResponseResult::getData)
                .orElse(Collections.emptyList())
                .stream()
                .collect(
                        Collectors.toMap(
                                User::getUserId,
                                Function.identity(),
                                (existing, replacement) -> existing));
    }
}
