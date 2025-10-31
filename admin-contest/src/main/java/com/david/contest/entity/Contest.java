package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("contests")
public class Contest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("description_md")
    private String descriptionMd;

    private String kind;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("is_visible")
    private Integer isVisible;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
   private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("registration_mode")
    private String registrationMode;

    @TableField("registration_start_time")
    private LocalDateTime registrationStartTime;

    @TableField("registration_end_time")
    private LocalDateTime registrationEndTime;

    @TableField("max_participants")
    private Integer maxParticipants;

    @TableField("penalty_per_wrong")
    private Integer penaltyPerWrong;

    @TableField("scoreboard_freeze_minutes")
    private Integer scoreboardFreezeMinutes;

    @TableField("hide_score_during_freeze")
    private Integer hideScoreDuringFreeze;
}
