package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.solution.enums.ContentType;
import com.david.usercontent.UserContentView;

import org.springframework.validation.annotation.Validated;

@Validated
public interface IUserContentViewService extends IService<UserContentView> {
    Long saveOrPassAndGetViewsNumber(Long userId, Long contentId, ContentType contentType);
	Long getViewsNumber(Long userId, Long contentId, ContentType contentType);
}
