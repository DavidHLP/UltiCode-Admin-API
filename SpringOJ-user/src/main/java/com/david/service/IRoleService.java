package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.entity.role.Role;

public interface IRoleService extends IService<Role> {
    Page<Role> pageRoles(int page, int size, String keyword, Integer status);
}
