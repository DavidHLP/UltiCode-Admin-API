package com.david.domain.dto;

import com.david.domain.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JudgeResult {
    private SubmissionStatus status;
    private String output;
    private String errorOutput;
    private long time; // 单位: ms
    private long memory; // 单位: KB
}
