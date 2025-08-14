package com.david.solution.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private List<SolutionCommentVo> children;
}
