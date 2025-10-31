package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("contest_participants")
public class ContestParticipant {

    @TableField("contest_id")
    private Long contestId;

    @TableField("user_id")
    private Long userId;

    @TableField("registered_at")
    private LocalDateTime registeredAt;
}
