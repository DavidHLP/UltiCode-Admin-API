package com.david.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.CodeTemplate;
import com.david.vo.CodeTemplateVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author david
 * @since 2025-07-31
 */
public interface IPCodeTemplateService extends IService<CodeTemplate> {
    List<CodeTemplateVo> getCodeTemplateVosByProblemId(Long problemId);
	CodeTemplate getCodeTemplateByProblemIdAndLanguage(Long problemId, String language);
}