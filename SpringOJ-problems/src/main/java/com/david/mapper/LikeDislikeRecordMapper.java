package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.calendar.LikeDislikeRecord;
import com.david.calendar.enums.ActionType;
import com.david.calendar.enums.TargetType;
import com.david.solution.UpDownCounts;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LikeDislikeRecordMapper extends BaseMapper<LikeDislikeRecord> {

    /** 根据用户与目标获取一条记录 */
    LikeDislikeRecord selectByUserAndTarget(
            @Param("userId") Long userId,
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId);

    /** 根据用户与目标删除记录 */
    int deleteByUserAndTarget(
            @Param("userId") Long userId,
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId);

    /** 统计目标的某一动作数量 */
    long countByTargetAndAction(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId,
            @Param("actionType") ActionType actionType);

    /** 插入一条记录（返回自增ID） */
    int insertRecord(LikeDislikeRecord record);

    /** 根据ID更新action与更新时间 */
    int updateActionById(
            @Param("id") Long id,
            @Param("actionType") ActionType actionType,
            @Param("updatedAt") LocalDateTime updatedAt);

	List<UpDownCounts> selectUpDownCountsByTargetTypeAndTargetIds(@Param("targetType") TargetType targetType , @Param("targetIds") List<Long> targetIds);
}
