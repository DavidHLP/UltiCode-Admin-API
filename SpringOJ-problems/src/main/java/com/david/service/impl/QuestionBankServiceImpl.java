package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.QuestionBankItemDto;
import com.david.dto.QuestionBankQueryDto;
import com.david.mapper.ProblemMapper;
import com.david.service.IQuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements IQuestionBankService {

    private final ProblemMapper problemMapper;

    @Override
    public Page<QuestionBankItemDto> getQuestionBankPage(QuestionBankQueryDto queryDto , Long userId) {
        Page<QuestionBankItemDto> page = new Page<>(queryDto.getPage(), queryDto.getSize());
        return problemMapper.findProblemsForQuestionBank(page, queryDto, userId);
    }
}
