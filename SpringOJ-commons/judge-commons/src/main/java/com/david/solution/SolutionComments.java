package com.david.solution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("solution_comments")
public class SolutionComments {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long solutionId;
    private Long userId;
    private String content;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private Integer upvotes;
    private Integer downvotes;
    private String status;
    private String meta;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
