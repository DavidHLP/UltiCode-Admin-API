package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.usercontent.UserContentView;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
public interface IUserContentViewService extends IService<UserContentView> {
	Boolean userHasViewedContent(@NotNull @Min(1) Long userId,
	                            @NotNull @Min(1) Long contentId);
}
