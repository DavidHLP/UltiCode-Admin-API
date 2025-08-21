package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.user.User;
import com.david.interfaces.UserServiceFeignClient;
import com.david.mapper.SolutionCommentMapper;
import com.david.service.ISolutionCommentService;
import com.david.solution.SolutionComments;
import com.david.solution.vo.SolutionCommentVo;
import com.david.exception.BizException;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Validated
public class SolutionCommentServiceImpl extends ServiceImpl<SolutionCommentMapper, SolutionComments>
        implements ISolutionCommentService {
    private final SolutionCommentMapper solutionCommentMapper;
    private final UserServiceFeignClient userServiceFeignClient;
    private static final String UNKNOWN_USER = "Unknown User";

    @Override
    public List<SolutionCommentVo> getSolutionCommentVos(Long solutionId) {
        // 输入参数校验（与接口层注解互补）
        validateSolutionId(solutionId);

        List<SolutionComments> allComments =
                solutionCommentMapper.selectCommentsBySolutionId(solutionId);
        if (allComments == null || allComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 评论数据完整性/一致性校验
        validateCommentsIntegrity(allComments);

        // 汇总需要查询的用户ID
        Set<Long> userIdsToFetch = extractUserIds(allComments);
        if (userIdsToFetch.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询并校验用户信息
        Map<Long, User> users = fetchUsers(userIdsToFetch);

        // 组装 CommentVo 映射
        Map<Long, SolutionCommentVo> commentVoMap = buildCommentVoMap(allComments, users);

        // 构建评论树
        List<SolutionCommentVo> rootComments = buildTree(commentVoMap);
        rootComments.sort((c1, c2) -> c2.getId().compareTo(c1.getId()));
        rootComments.forEach(this::sortChildrenRecursively);
        return rootComments;
    }

    /**
     * 辅助方法：递归地对子评论按ID升序排序（保证对话顺序）
     *
     * @param parentComment 父评论
     */
    private void sortChildrenRecursively(SolutionCommentVo parentComment) {
        if (parentComment.getChildren() != null && !parentComment.getChildren().isEmpty()) {
            parentComment.getChildren().sort(Comparator.comparing(SolutionCommentVo::getId));
            parentComment.getChildren().forEach(this::sortChildrenRecursively);
        }
    }

    private void validateSolutionId(Long solutionId) {
        if (solutionId == null || solutionId < 1) {
            throw new BizException(ResponseCode.RC400.getCode(), "题解ID必须为正数，当前值：" + solutionId);
        }
    }

    private Set<Long> extractUserIds(List<SolutionComments> allComments) {
        return allComments.stream()
                .flatMap(c -> Stream.of(c.getUserId(), c.getReplyToUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map<Long, User> fetchUsers(Set<Long> userIdsToFetch) {
        ResponseResult<List<User>> userResp =
                userServiceFeignClient.getUserByIds(new ArrayList<>(userIdsToFetch));
        if (userResp == null) {
            throw new BizException(ResponseCode.RC500.getCode(), "用户服务未返回响应");
        }
        if (!Objects.equals(userResp.getCode(), ResponseCode.RC200.getCode())) {
            throw new BizException(userResp.getCode(), "批量查询用户失败: " + userResp.getMessage());
        }
        List<User> userList = userResp.getData();
        if (userList == null) {
            throw new BizException(ResponseCode.RC500.getCode(), "用户服务返回空数据，用户ID集合：" + userIdsToFetch);
        }
        Map<Long, User> users = userList.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
        Set<Long> missing = new HashSet<>(userIdsToFetch);
        missing.removeAll(users.keySet());
        if (!missing.isEmpty()) {
            throw new BizException(ResponseCode.RC400.getCode(), "无法获取以下用户信息：" + missing);
        }
        return users;
    }

    private Map<Long, SolutionCommentVo> buildCommentVoMap(List<SolutionComments> allComments, Map<Long, User> users) {
        return allComments.stream()
                .map(comment -> {
                    User author = users.get(comment.getUserId());
                    String replyToUsername = null;
                    if (comment.getReplyToUserId() != null) {
                        User replyToUser = users.get(comment.getReplyToUserId());
                        replyToUsername = replyToUser != null ? replyToUser.getUsername() : null;
                    }
                    return SolutionCommentVo.builder()
                            .id(comment.getId())
                            .solutionId(comment.getSolutionId())
                            .userId(comment.getUserId())
                            .content(comment.getContent())
                            .parentId(comment.getParentId())
                            .rootId(comment.getRootId())
                            .replyToUserId(comment.getReplyToUserId())
                            .upvotes(comment.getUpvotes())
                            .downvotes(comment.getDownvotes())
                            .children(new ArrayList<>())
                            .avatar(author != null ? author.getAvatar() : null)
                            .username(author != null ? author.getUsername() : UNKNOWN_USER)
                            .replyToUsername(replyToUsername)
                            .build();
                })
                .collect(Collectors.toMap(SolutionCommentVo::getId, Function.identity()));
    }

    private List<SolutionCommentVo> buildTree(Map<Long, SolutionCommentVo> commentVoMap) {
        List<SolutionCommentVo> rootComments = new ArrayList<>();
        commentVoMap.values().forEach(vo -> {
            Long parentId = vo.getParentId();
            if (parentId == null) {
                rootComments.add(vo);
            } else {
                SolutionCommentVo parentVo = commentVoMap.get(parentId);
                if (parentVo != null) {
                    parentVo.getChildren().add(vo);
                }
            }
        });
        return rootComments;
    }

    private void validateCommentsIntegrity(List<SolutionComments> allComments) {
        Set<Long> seenIds = new HashSet<>();
        List<Long> duplicateIds = new ArrayList<>();
        for (SolutionComments c : allComments) {
            if (c.getId() == null || c.getId() < 1) {
                throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "存在非法评论ID，当前值：" + c.getId());
            }
            if (!seenIds.add(c.getId())) {
                duplicateIds.add(c.getId());
            }
            if (c.getUserId() == null || c.getUserId() < 1) {
                throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "评论ID " + c.getId() + " 的用户ID非法，当前值：" + c.getUserId());
            }
            if (c.getContent() == null || c.getContent().trim().isEmpty()) {
                throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "评论ID " + c.getId() + " 的内容不能为空");
            }
            if (c.getParentId() == null) {
                if (c.getRootId() != null) {
                    throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "根评论的rootId必须为空，评论ID：" + c.getId());
                }
            } else {
                if (c.getParentId() < 1) {
                    throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "评论ID " + c.getId() + " 的parentId必须为正数，当前值：" + c.getParentId());
                }
                if (c.getRootId() == null || c.getRootId() < 1) {
                    throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "子评论必须提供有效rootId，评论ID：" + c.getId());
                }
            }
            if (c.getReplyToUserId() != null && c.getReplyToUserId() < 1) {
                throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "评论ID " + c.getId() + " 的被回复用户ID必须为正数，当前值：" + c.getReplyToUserId());
            }
            if (c.getUpvotes() != null && c.getUpvotes() < 0) {
                throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "评论ID " + c.getId() + " 的点赞数不能为负，当前值：" + c.getUpvotes());
            }
            if (c.getDownvotes() != null && c.getDownvotes() < 0) {
                throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "评论ID " + c.getId() + " 的点踩数不能为负，当前值：" + c.getDownvotes());
            }
        }
        if (!duplicateIds.isEmpty()) {
            throw new BizException(ResponseCode.BUSINESS_ERROR.getCode(), "存在重复的评论ID：" + duplicateIds);
        }
    }

}
