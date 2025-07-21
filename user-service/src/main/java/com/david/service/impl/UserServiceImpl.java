package com.david.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.role.Role;
import com.david.entity.user.User;
import com.david.entity.user.UserRole;
import com.david.mapper.UserMapper;
import com.david.mapper.UserRoleMapper;
import com.david.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author david
 * @since 2025-07-20
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserRoleMapper userRoleMapper;

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
        boolean result = super.save(user);
        if (result && user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<Long> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());
            for (Long roleId : roleIds) {
                userRoleMapper.insert(new UserRole(user.getUserId(), roleId));
            }
        }
        return result;
    }

    @Override
    @Transactional
    public boolean updateById(User user) {
        boolean result = super.updateById(user);
        if (result) {
            userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", user.getUserId()));
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                List<Long> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());
                for (Long roleId : roleIds) {
                    userRoleMapper.insert(new UserRole(user.getUserId(), roleId));
                }
            }
        }
        return result;
    }
}