package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.QuestionBankItemDto;
import com.david.dto.QuestionBankQueryDto;

public interface IQuestionBankService {
    Page<QuestionBankItemDto> getQuestionBankPage(QuestionBankQueryDto queryDto , Long userId);
}
