package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.QuestionBankItemDto;
import com.david.dto.QuestionBankQueryDto;
import com.david.service.IQuestionBankService;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/question-bank/api")
public class QuestionBankController extends BaseController {

    private final IQuestionBankService questionBankService;

    @GetMapping
    public ResponseResult<Page<QuestionBankItemDto>> getQuestionBank(QuestionBankQueryDto queryDto) {
        Page<QuestionBankItemDto> questionBankPage = questionBankService.getQuestionBankPage(queryDto,getCurrentUserId());
        return ResponseResult.success("成功获取题库列表", questionBankPage);
    }
}
