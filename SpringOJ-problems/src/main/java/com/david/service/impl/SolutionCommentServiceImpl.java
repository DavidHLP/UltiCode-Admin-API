package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.commons.redis.cache.annotation.RedisCacheable;
import com.david.commons.redis.cache.annotation.RedisEvict;
import com.david.entity.user.AuthUser;
import com.david.exception.BizException;
import com.david.interfaces.UserServiceFeignClient;
import com.david.mapper.SolutionCommentMapper;
import com.david.service.ISolutionCommentService;
import com.david.solution.SolutionComments;
import com.david.solution.vo.SolutionCommentVo;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @RedisCacheable(
            key = "'solutionComment:getSolutionCommentVos:' + #solutionId",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = Page.class)
    public List<SolutionCommentVo> getSolutionCommentVos(Long solutionId) {
        if (solutionId == null) {
            throw new BizException(ResponseCode.RC400.getCode(), "题解ID不能为空");
        }
        if (solutionId < 1) {
            throw new BizException(ResponseCode.RC400.getCode(), "题解ID必须为正数，当前值：" + solutionId);
        }
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

        ResponseResult<List<AuthUser>> userResp =
                userServiceFeignClient.getUserByIds(new ArrayList<>(userIdsToFetch));
        if (userResp == null) {
            throw new BizException(ResponseCode.RC500.getCode(), "用户服务返回为空");
        }
        if (userResp.getCode() == null
                || !userResp.getCode().equals(ResponseCode.RC200.getCode())) {
            Integer code =
                    userResp.getCode() == null ? ResponseCode.RC500.getCode() : userResp.getCode();
            String msg = userResp.getMessage() == null ? "用户服务调用失败" : userResp.getMessage();
            throw new BizException(code, "查询用户信息失败：" + msg);
        }
        List<AuthUser> userList = userResp.getData();
        if (userList == null) {
            throw new BizException(ResponseCode.RC500.getCode(), "用户服务返回数据为空");
        }

        Map<Long, AuthUser> users =
                userList.stream()
                        .collect(Collectors.toMap(AuthUser::getUserId, Function.identity()));

        Map<Long, SolutionCommentVo> commentVoMap =
                allComments.stream()
                        .map(
                                comment -> {
                                    AuthUser author = users.get(comment.getUserId());
                                    String replyToUsername = null;
                                    if (comment.getReplyToUserId() != null) {
                                        AuthUser replyToUser =
                                                users.get(comment.getReplyToUserId());
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
        // 数据有效性校验（服务层补充判断）
        for (SolutionCommentVo c : rootComments) {
            if (c.getId() == null) {
                throw new BizException(
                        ResponseCode.RC500.getCode(), "评论数据异常：ID为空，题解ID：" + solutionId);
            }
            if (!Objects.equals(c.getSolutionId(), solutionId)) {
                throw new BizException(
                        ResponseCode.RC500.getCode(),
                        "评论数据异常：记录题解ID与请求不一致，记录值："
                                + c.getSolutionId()
                                + "，请求值："
                                + solutionId
                                + "，评论ID："
                                + c.getId());
            }
            if (c.getUpvotes() != null && c.getUpvotes() < 0) {
                throw new BizException(
                        ResponseCode.RC400.getCode(),
                        "点赞数不能为负数，当前值：" + c.getUpvotes() + "，评论ID：" + c.getId());
            }
            if (c.getDownvotes() != null && c.getDownvotes() < 0) {
                throw new BizException(
                        ResponseCode.RC400.getCode(),
                        "点踩数不能为负数，当前值：" + c.getDownvotes() + "，评论ID：" + c.getId());
            }
            //            if (!c.isHierarchyValid()) {
            //                throw new BizException(ResponseCode.RC400.getCode(),
            //                        "评论层级关系不合法（根评论rootId应为空；子评论必须提供rootId），评论ID：" + c.getId());
            //            }
        }
        // 检查ID是否唯一，避免构建映射时冲突
        long distinctIdCount = allComments.stream().map(SolutionComments::getId).distinct().count();
        if (distinctIdCount != allComments.size()) {
            Map<Long, Long> idCount =
                    allComments.stream()
                            .collect(
                                    Collectors.groupingBy(
                                            SolutionComments::getId, Collectors.counting()));
            List<Long> duplicates =
                    idCount.entrySet().stream()
                            .filter(e -> e.getValue() > 1)
                            .map(Map.Entry::getKey)
                            .toList();
            throw new BizException(ResponseCode.RC500.getCode(), "评论数据异常：存在重复ID " + duplicates);
        }
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

    @Override
    @Transactional
    public boolean save(SolutionComments entity) {
        return solutionCommentMapper.insert(entity) > 0;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = "'solutionComment:getSolutionCommentVos:' + #entity.getSolutionId()",
            keyPrefix = "springoj:cache:")
    public boolean updateById(SolutionComments entity) {
        return solutionCommentMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = "'solutionComment:getSolutionCommentVos:' + #entity.getSolutionId()",
            keyPrefix = "springoj:cache:")
    public boolean removeById(SolutionComments entity) {
        return solutionCommentMapper.deleteById(entity) > 0;
    }
}
