package com.david.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.role.Role;
import com.david.mapper.RoleMapper;
import com.david.service.IRoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {
	private final RoleMapper roleMapper;
	@Override
	public Page<Role> pageRoles(int page, int size, String keyword, Integer status) {
		Page<Role> rolePage = new Page<>(page, size);
		List<Role> roles = roleMapper.selectRolePage(rolePage, keyword, status);
		rolePage.setRecords(roles);
		return rolePage;
	}
}
