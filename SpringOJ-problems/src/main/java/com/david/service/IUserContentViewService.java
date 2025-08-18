package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.usercontent.UserContentView;

public interface IUserContentViewService extends IService<UserContentView> {
	Boolean userHasViewedContent(Long userId, Long contentId);
}
