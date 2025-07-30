package com.david.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.CodeTemplate;
import com.david.mapper.CodeTemplateMapper;
import com.david.service.IPCodeTemplateService;
import com.david.vo.CodeTemplateVo;

@Service
public class CodeTemplateServiceImpl extends ServiceImpl<CodeTemplateMapper, CodeTemplate>
		implements
			IPCodeTemplateService {
	@Override
	public List<CodeTemplateVo> getCodeTemplateVosByProblemId(Long problemId) {
		return this
				.lambdaQuery().eq(CodeTemplate::getProblemId, problemId).list().stream().map(template -> CodeTemplateVo
						.builder().code(template.getSolutionTemplate()).language(template.getLanguage()).build())
				.toList();
	}

	@Override
	public CodeTemplate getCodeTemplateByProblemIdAndLanguage(Long problemId, String language) {
		return this.lambdaQuery().eq(CodeTemplate::getProblemId, problemId).eq(CodeTemplate::getLanguage, language)
				.one();
	}
}
