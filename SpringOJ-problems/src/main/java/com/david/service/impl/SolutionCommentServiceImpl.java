package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.user.User;
import com.david.interfaces.UserServiceFeignClient;
import com.david.mapper.SolutionCommentMapper;
import com.david.service.ISolutionCommentService;
import com.david.solution.SolutionComments;
import com.david.vo.SolutionCommentVo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Map<Long, User> users =
                userServiceFeignClient
                        .getUserByIds(
                                allComments.stream()
                                        .map(SolutionComments::getUserId)
                                        .distinct()
                                        .toList())
                        .getData()
                        .stream()
                        .collect(Collectors.toMap(User::getUserId, Function.identity()));
        Map<Long, SolutionCommentVo> commentVoMap =
                allComments.stream()
                        .map(
                                comment ->
                                        SolutionCommentVo.builder()
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
                                                .avatar(users.get(comment.getUserId()).getAvatar())
                                                .username(
                                                        users.get(comment.getUserId())
                                                                .getUsername())
                                                .replyToUsername(
                                                        users.get(comment.getReplyToUserId())
                                                                .getUsername())
                                                .build())
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
