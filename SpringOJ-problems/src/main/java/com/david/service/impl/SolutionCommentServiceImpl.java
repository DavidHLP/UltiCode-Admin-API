package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.user.User;
import com.david.interfaces.UserServiceFeignClient;
import com.david.mapper.SolutionCommentMapper;
import com.david.service.ISolutionCommentService;
import com.david.solution.SolutionComments;
import com.david.solution.vo.SolutionCommentVo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SolutionCommentServiceImpl extends ServiceImpl<SolutionCommentMapper, SolutionComments>
        implements ISolutionCommentService {
    private final SolutionCommentMapper solutionCommentMapper;
    private final UserServiceFeignClient userServiceFeignClient;

    @Override
    public List<SolutionCommentVo> getSolutionCommentVos(Long solutionId) {
        List<SolutionComments> allComments =
                solutionCommentMapper.selectCommentsBySolutionId(solutionId);
        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIdsToFetch =
                allComments.stream()
                        .flatMap(
                                comment ->
                                        Stream.of(comment.getUserId(), comment.getReplyToUserId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        if (userIdsToFetch.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, User> users =
                userServiceFeignClient
                        .getUserByIds(new ArrayList<>(userIdsToFetch))
                        .getData()
                        .stream()
                        .collect(Collectors.toMap(User::getUserId, Function.identity()));

        Map<Long, SolutionCommentVo> commentVoMap =
                allComments.stream()
                        .map(
                                comment -> {
                                    User author = users.get(comment.getUserId());
                                    String replyToUsername = null;
                                    if (comment.getReplyToUserId() != null) {
                                        User replyToUser = users.get(comment.getReplyToUserId());
                                        if (replyToUser != null) {
                                            replyToUsername = replyToUser.getUsername();
                                        }
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
                                            .username(
                                                    author != null
                                                            ? author.getUsername()
                                                            : "Unknown User")
                                            .replyToUsername(replyToUsername)
                                            .build();
                                })
                        .collect(Collectors.toMap(SolutionCommentVo::getId, Function.identity()));

        List<SolutionCommentVo> rootComments = new ArrayList<>();
        commentVoMap
                .values()
                .forEach(
                        commentVo -> {
                            Long parentId = commentVo.getParentId();
                            if (parentId == null) {
                                rootComments.add(commentVo);
                            } else {
                                SolutionCommentVo parentVo = commentVoMap.get(parentId);
                                if (parentVo != null) {
                                    parentVo.getChildren().add(commentVo);
                                }
                            }
                        });
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
}
