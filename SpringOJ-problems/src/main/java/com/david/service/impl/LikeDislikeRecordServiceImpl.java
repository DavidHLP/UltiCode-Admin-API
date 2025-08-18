package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.calendar.LikeDislikeRecord;
import com.david.calendar.enums.ActionType;
import com.david.calendar.enums.TargetType;
import com.david.calendar.vo.LikeDislikeRecordVo;
import com.david.mapper.LikeDislikeRecordMapper;
import com.david.service.ILikeDislikeRecordService;
import com.david.solution.UpDownCounts;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 点赞点踩记录服务实现类 实现用户交互逻辑，包含状态转换和防重复操作 */
@Service
@RequiredArgsConstructor
public class LikeDislikeRecordServiceImpl
        extends ServiceImpl<LikeDislikeRecordMapper, LikeDislikeRecord>
        implements ILikeDislikeRecordService {
    private final LikeDislikeRecordMapper likeDislikeRecordMapper;

    @Override
    public LikeDislikeRecordVo getUserActionStatus(
            Long userId, TargetType targetType, Long targetId) {
        // 获取用户当前操作状态（XML 自定义 SQL）
        LikeDislikeRecord record =
                likeDislikeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);

        // 获取统计数据（VO）
        LikeDislikeRecordVo stats = getStats(targetType, targetId);

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
        // 取消用户对目标的操作（XML 自定义 SQL）
        likeDislikeRecordMapper.deleteByUserAndTarget(userId, targetType, targetId);
        return getUserActionStatus(userId, targetType, targetId);
    }

    @Override
    public LikeDislikeRecordVo getStats(TargetType targetType, Long targetId) {
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
	    return likeDislikeRecordMapper.selectUpDownCountsByTargetTypeAndTargetIds(
	            targetType, targetIds);
    }
}
