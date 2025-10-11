package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.entity.role.Role;
import com.david.mapper.RoleMapper;
import com.david.service.IRoleService;

import io.github.davidhlp.spring.cache.redis.annotation.RedisCacheEvict;
import io.github.davidhlp.spring.cache.redis.annotation.RedisCacheable;
import io.github.davidhlp.spring.cache.redis.annotation.RedisCaching;

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
            key = "'pageRoles:' + #page + ':' + #size + ':' + #status",
            ttl = 1800, // 30分钟缓存
            type = Page.class,
            cacheNames = "role",
            condition = "#keyword == null")
    public Page<Role> pageRoles(int page, int size, String keyword, Boolean status) {
        Page<Role> rolePage = new Page<>(page, size);
        List<Role> roles = roleMapper.selectRolePage(rolePage, keyword, status);
        rolePage.setRecords(roles);
        return rolePage;
    }

    @Override
    @RedisCacheable(
            key = "'list'",
            ttl = 1800, // 30分钟缓存
            type = List.class,
            cacheNames = "role")
    public List<Role> list() {
        return roleMapper.selectList(null);
    }

    @Override
    @RedisCacheable(key = "'getById:' + #id", ttl = 1800, type = Role.class, cacheNames = "role")
    public Role getById(Serializable id) {
        return roleMapper.selectById(id);
    }

    @Override
    @RedisCaching(
            redisCacheEvict = {
                @RedisCacheEvict(cacheNames = "role", allEntries = true),
                @RedisCacheEvict(cacheNames = "user", allEntries = true)
            })
    public boolean save(Role entity) {
        return roleMapper.insert(entity) > 0;
    }

    @Override
    @RedisCaching(
		    redisCacheEvict = {
				    @RedisCacheEvict(cacheNames = "role", allEntries = true),
				    @RedisCacheEvict(cacheNames = "user", allEntries = true)
		    })
    public boolean updateById(Role entity) {
        return roleMapper.updateById(entity) > 0;
    }

    @Override
    @RedisCaching(
		    redisCacheEvict = {
				    @RedisCacheEvict(cacheNames = "role", allEntries = true),
				    @RedisCacheEvict(cacheNames = "user", allEntries = true)
		    })
    public boolean removeById(Serializable id) {
        return roleMapper.deleteById(id) > 0;
    }
}
