package com.david.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SolutionCommentVo {
    private Long id;
    private Long solutionId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private String replyToUsername;
    private Integer upvotes;
    private Integer downvotes;
    private LocalDateTime createdAt;
    private List<SolutionCommentVo> children;
}
