package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("vw_user_problem_best")
public class UserProblemBestView {

    @TableField("user_id")
    private Long userId;

    @TableField("problem_id")
    private Long problemId;

    @TableField("first_ac_time")
    private LocalDateTime firstAcTime;

    @TableField("best_score")
    private Integer bestScore;
}
