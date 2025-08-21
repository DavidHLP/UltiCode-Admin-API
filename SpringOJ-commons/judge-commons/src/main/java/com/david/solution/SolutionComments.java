package com.david.solution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("solution_comments")
public class SolutionComments {
    /**
     * 校验分组：创建/更新/删除
     */
    public interface Create {}
    public interface Update {}
    public interface Delete {}

    @TableId(type = IdType.AUTO)
    @NotNull(message = "评论ID不能为空", groups = {Update.class, Delete.class})
    @Min(value = 1, message = "评论ID必须为正数", groups = {Update.class, Delete.class})
    private Long id;

    @NotNull(message = "题解ID不能为空", groups = {Create.class})
    @Min(value = 1, message = "题解ID必须为正数", groups = {Create.class, Update.class})
    private Long solutionId;

    @NotNull(message = "用户ID不能为空", groups = {Create.class})
    @Min(value = 1, message = "用户ID必须为正数", groups = {Create.class, Update.class})
    private Long userId;

    @NotBlank(message = "评论内容不能为空", groups = {Create.class, Update.class})
    @Size(max = 1000, message = "评论内容长度不能超过1000字", groups = {Create.class, Update.class})
    private String content;

    @Min(value = 1, message = "父评论ID必须为正数", groups = {Create.class, Update.class})
    private Long parentId;

    @Min(value = 1, message = "根评论ID必须为正数", groups = {Create.class, Update.class})
    private Long rootId;

    @Min(value = 1, message = "被回复用户ID必须为正数", groups = {Create.class, Update.class})
    private Long replyToUserId;

    @Min(value = 0, message = "点赞数不能为负", groups = {Create.class, Update.class})
    private Integer upvotes;

    @Min(value = 0, message = "点踩数不能为负", groups = {Create.class, Update.class})
    private Integer downvotes;

    @Size(max = 20, message = "状态长度不能超过20", groups = {Create.class, Update.class})
    private String status;

    @Size(max = 1024, message = "meta长度不能超过1024", groups = {Create.class, Update.class})
    private String meta;

    /**
     * 交叉字段校验：
     * - 当 parentId 为空时，rootId 也必须为空（表示根评论）
     * - 当 parentId 不为空时，rootId 必须非空（表示子评论）
     */
    @AssertTrue(message = "根评论的rootId必须为空；子评论必须提供rootId", groups = {Create.class, Update.class})
    public boolean isHierarchyValid() {
        if (parentId == null) {
            return rootId == null;
        } else {
            return rootId != null;
        }
    }
}
