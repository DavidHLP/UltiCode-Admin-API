package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.entity.user.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author david
 * @since 2025-07-20
 */
public interface IUserService extends IService<User> {
    /**
     * 分页查询用户（按关键字匹配用户名或邮箱）
     * @param page 第几页（从1开始）
     * @param size 每页大小
     * @param keyword 可选关键字（用户名/邮箱 模糊匹配）
     * @param roleId 可选角色ID（根据角色筛选）
     * @return 分页数据
     */
    Page<User> pageUsers(int page, int size, String keyword, Long roleId);
}
