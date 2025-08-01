package com.david.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolutionCommentDto {
    private Long solutionId;
    private String content;
    private Long parentId;
    private Long replyToUserId;
}
