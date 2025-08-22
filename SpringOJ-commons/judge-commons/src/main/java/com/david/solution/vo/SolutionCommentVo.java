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

    /**
     * 交叉字段校验：
     * - 当 parentId 为空时，rootId 也必须为空（表示根评论）
     * - 当 parentId 不为空时，rootId 必须非空（表示子评论）
     */
//    @AssertTrue(message = "根评论的rootId必须为空；子评论必须提供rootId")
//    public boolean isHierarchyValid() {
//        if (parentId == null) {
//            return rootId == null;
//        } else {
//            return rootId != null;
//        }
//    }
}
