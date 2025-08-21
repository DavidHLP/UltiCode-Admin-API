package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.calendar.LikeDislikeRecord;
import com.david.calendar.enums.ActionType;
import com.david.calendar.enums.TargetType;
import com.david.calendar.vo.LikeDislikeRecordVo;
import com.david.mapper.LikeDislikeRecordMapper;
import com.david.service.ILikeDislikeRecordService;
import com.david.solution.UpDownCounts;
import com.david.exception.BizException;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/** 点赞点踩记录服务实现类 实现用户交互逻辑，包含状态转换和防重复操作 */
@Service
@Validated
@RequiredArgsConstructor
public class LikeDislikeRecordServiceImpl
        extends ServiceImpl<LikeDislikeRecordMapper, LikeDislikeRecord>
        implements ILikeDislikeRecordService {
    private final LikeDislikeRecordMapper likeDislikeRecordMapper;

    @Override
    public LikeDislikeRecordVo getUserActionStatus(
            Long userId, TargetType targetType, Long targetId) {
        // 基础参数校验
        if (userId == null || userId < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), "用户ID必须为正数，当前值：" + userId);
        }
        if (targetType == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标类型不能为空");
        }
        if (targetId == null || targetId < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标ID必须为正数，当前值：" + targetId);
        }
        // 获取用户当前操作状态（XML 自定义 SQL）
        LikeDislikeRecord record =
                likeDislikeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);

        // 获取统计数据（VO）
        LikeDislikeRecordVo stats = getStats(targetType, targetId);
        if (stats == null) {
            throw BizException.of(ResponseCode.RC500.getCode(), "统计数据为空");
        }
        if (stats.getLikeCount() < 0 || stats.getDislikeCount() < 0 || stats.getTotalCount() < 0) {
            throw BizException.of(ResponseCode.RC500.getCode(), "统计数据异常：出现负数");
        }

        return LikeDislikeRecordVo.builder()
                .userAction(record != null ? record.getActionType().name() : "NONE")
                .likeCount(stats.getLikeCount())
                .dislikeCount(stats.getDislikeCount())
                .totalCount(stats.getTotalCount())
                .build();
    }

    @Override
    @Transactional
    public LikeDislikeRecordVo performAction(
            Long userId, TargetType targetType, Long targetId, ActionType actionType) {
        // 基础参数校验
        if (userId == null || userId < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), "用户ID必须为正数，当前值：" + userId);
        }
        if (targetType == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标类型不能为空");
        }
        if (targetId == null || targetId < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标ID必须为正数，当前值：" + targetId);
        }
        if (actionType == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), "操作类型不能为空");
        }
        LikeDislikeRecord existingRecord =
                likeDislikeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);

        if (existingRecord != null) {
            if (existingRecord.getActionType() == actionType) {
                // 重复操作，取消当前操作（XML 自定义 SQL）
                likeDislikeRecordMapper.deleteByUserAndTarget(userId, targetType, targetId);
            } else {
                // 切换操作类型（XML 自定义 SQL）
                likeDislikeRecordMapper.updateActionById(
                        existingRecord.getId(), actionType, LocalDateTime.now());
            }
        } else {
            // 新操作（XML 自定义 SQL）
            LikeDislikeRecord newRecord =
                    LikeDislikeRecord.builder()
                            .userId(userId)
                            .targetType(targetType)
                            .targetId(targetId)
                            .actionType(actionType)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
            likeDislikeRecordMapper.insertRecord(newRecord);
        }

        return getUserActionStatus(userId, targetType, targetId);
    }

    @Override
    @Transactional
    public LikeDislikeRecordVo cancelAction(Long userId, TargetType targetType, Long targetId) {
        if (userId == null || userId < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), "用户ID必须为正数，当前值：" + userId);
        }
        if (targetType == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标类型不能为空");
        }
        if (targetId == null || targetId < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标ID必须为正数，当前值：" + targetId);
        }
        // 取消用户对目标的操作（XML 自定义 SQL）
        likeDislikeRecordMapper.deleteByUserAndTarget(userId, targetType, targetId);
        return getUserActionStatus(userId, targetType, targetId);
    }

    @Override
    public LikeDislikeRecordVo getStats(TargetType targetType, Long targetId) {
        if (targetType == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标类型不能为空");
        }
        if (targetId == null || targetId < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标ID必须为正数，当前值：" + targetId);
        }
        // 统计点赞与点踩数量（XML 自定义 SQL）
        long likeCount =
                likeDislikeRecordMapper.countByTargetAndAction(
                        targetType, targetId, ActionType.LIKE);
        long dislikeCount =
                likeDislikeRecordMapper.countByTargetAndAction(
                        targetType, targetId, ActionType.DISLIKE);

        return LikeDislikeRecordVo.builder()
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .totalCount(likeCount + dislikeCount)
                .build();
    }

    @Override
    public List<UpDownCounts> getUpDownCountsByTargetTypeAndTargetIds(
            TargetType targetType, List<Long> targetIds) {
        if (targetType == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标类型不能为空");
        }
        if (targetIds == null || targetIds.isEmpty()) {
            throw BizException.of(ResponseCode.RC400.getCode(), "目标ID列表不能为空");
        }
        for (int i = 0; i < targetIds.size(); i++) {
            Long id = targetIds.get(i);
            if (id == null || id < 1) {
                throw BizException.of(ResponseCode.RC400.getCode(), "目标ID必须为正数，索引=" + i + ", 当前值：" + id);
            }
        }
        List<UpDownCounts> result = likeDislikeRecordMapper.selectUpDownCountsByTargetTypeAndTargetIds(
                targetType, targetIds);
        if (result == null) {
            throw BizException.of(ResponseCode.RC500.getCode(), "目标统计结果为空");
        }
        return result;
    }
}
