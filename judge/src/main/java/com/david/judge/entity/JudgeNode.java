package com.david.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("judge_nodes")
public class JudgeNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String status;

    @TableField("runtime_info")
    private String runtimeInfo;

    @TableField("last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
