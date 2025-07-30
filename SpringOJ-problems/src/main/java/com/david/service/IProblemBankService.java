package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.ProblemBankQueryDto;
import com.david.vo.ProblemBankItemVo;
import com.david.vo.SubmissionCalendarVo;

import java.util.List;

public interface IProblemBankService {
    Page<ProblemBankItemVo> getQuestionBankPage(ProblemBankQueryDto queryDto , Long userId);

	List<SubmissionCalendarVo> getSubmissionCalendar(Long currentUserId);
}
