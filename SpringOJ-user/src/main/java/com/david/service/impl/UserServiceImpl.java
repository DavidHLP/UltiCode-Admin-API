package com.david.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.role.Role;
import com.david.entity.user.User;
import com.david.entity.user.UserRole;
import com.david.mapper.UserMapper;
import com.david.mapper.UserRoleMapper;
import com.david.service.IUserService;

import lombok.RequiredArgsConstructor;

/**
 * @author david
 * @since 2025-07-20
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

	private final UserRoleMapper userRoleMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	public User getById(java.io.Serializable id) {
		User user = super.getById(id);
		if (user != null) {
			user.setRoles(baseMapper.findRolesByUserId(user.getUserId()));
		}
		return user;
	}

	@Override
	public List<User> list() {
		List<User> users = super.list();
		users.forEach(user -> user.setRoles(baseMapper.findRolesByUserId(user.getUserId())));
		return users;
	}

	@Override
	@Transactional
	public boolean save(User user) {
		// 设置默认值
		if (user.getStatus() == null) {
			user.setStatus(1); // 默认激活状态
		}
		if (user.getCreateTime() == null) {
			user.setCreateTime(LocalDateTime.now()); // 设置创建时间
		}

		// 密码加密处理
		if (user.getPassword() != null && !user.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}

		boolean result = super.save(user);
		if (result && user.getRoles() != null && !user.getRoles().isEmpty()) {
			List<Long> roleIds = user.getRoles().stream().map(Role::getId).toList();
			for (Long roleId : roleIds) {
				userRoleMapper.insert(new UserRole(user.getUserId(), roleId));
			}
		}
		return result;
	}

	@Override
	@Transactional
	public boolean updateById(User user) {
		// 如果密码有更新，则进行加密
		if (user.getPassword() != null && !user.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}

		boolean result = super.updateById(user);
		if (result) {
			userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", user.getUserId()));
			if (user.getRoles() != null && !user.getRoles().isEmpty()) {
				List<Long> roleIds = user.getRoles().stream().map(Role::getId).toList();
				for (Long roleId : roleIds) {
					userRoleMapper.insert(new UserRole(user.getUserId(), roleId));
				}
			}
		}
		return result;
	}
}