package com.david.calendar.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeDislikeRecordVo {

    /**
     * 用户当前对目标的操作
     * 可取值："NONE" | "LIKE" | "DISLIKE"
     * 当仅用于统计（如 /stats 接口）时该字段可为 null
     */
    private String userAction;

    /** 点赞数量 */
    private long likeCount;

    /** 点踩数量 */
    private long dislikeCount;

    /** 总互动数（likeCount + dislikeCount） */
    private long totalCount;
}
