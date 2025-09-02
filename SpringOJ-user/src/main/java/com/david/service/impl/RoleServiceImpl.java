package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.commons.redis.cache.annotation.RedisCacheable;
import com.david.commons.redis.cache.annotation.RedisEvict;
import com.david.entity.role.Role;
import com.david.mapper.RoleMapper;
import com.david.service.IRoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {
    private final RoleMapper roleMapper;

    @Override
    @RedisCacheable(
            key =
                    "'role:pageRoles:' + #page + ':' + #size + ':' + (#keyword != null ? #keyword : '') + ':' + (#status != null ? #status : '')",
            ttl = 1800, // 30分钟缓存
            type = Page.class,
            keyPrefix = "springoj:cache:")
    public Page<Role> pageRoles(int page, int size, String keyword, Integer status) {
        Page<Role> rolePage = new Page<>(page, size);
        List<Role> roles = roleMapper.selectRolePage(rolePage, keyword, status);
        rolePage.setRecords(roles);
        return rolePage;
    }

    @Override
    @RedisCacheable(
            key = "'role:list'",
            ttl = 1800, // 30分钟缓存
            type = List.class,
            keyPrefix = "springoj:cache:")
    public List<Role> list() {
        return roleMapper.selectList(null);
    }

    @Override
    @RedisCacheable(
            key = "'role:getById:' + #id",
            ttl = 1800, // 30分钟缓存
            type = Role.class,
            keyPrefix = "springoj:cache:")
    public Role getById(Serializable id) {
        return roleMapper.selectById(id);
    }

    @Override
    @RedisEvict(keyPrefix = "springoj:cache:", allEntries = true, keys = "role:")
    public boolean save(Role entity) {
        return roleMapper.insert(entity) > 0;
    }

    @Override
    @RedisEvict(keyPrefix = "springoj:cache:", allEntries = true, keys = "role:")
    public boolean updateById(Role entity) {
        return roleMapper.updateById(entity) > 0;
    }

    @Override
    @RedisEvict(keyPrefix = "springoj:cache:", allEntries = true, keys = "role:")
    public boolean removeById(Serializable id) {
        return roleMapper.deleteById(id) > 0;
    }
}
