//package com.david.service.impl;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.david.commons.redis.cache.annotation.RedisCacheable;
//import com.david.commons.redis.cache.annotation.RedisEvict;
//import com.david.entity.user.AuthUser;
//import com.david.entity.user.UserRole;
//import com.david.mapper.RoleMapper;
//import com.david.mapper.UserMapper;
//import com.david.mapper.UserRoleMapper;
//import com.david.service.IUserService;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * @author david
// * @since 2025-07-20
// */
//@Service
//@RequiredArgsConstructor
//public class UserServiceImpl extends ServiceImpl<UserMapper, AuthUser> implements IUserService {
//
//    private final UserRoleMapper userRoleMapper;
//    private final PasswordEncoder passwordEncoder;
//    private final UserMapper userMapper;
//    private final RoleMapper roleMapper;
//
//    /**
//     * 分页查询用户（按关键字匹配用户名或邮箱，可选按角色筛选）
//     *
//     * @param page 第几页（从1开始）
//     * @param size 每页大小
//     * @param keyword 可选关键字（用户名/邮箱 模糊匹配）
//     * @param roleId 可选角色ID（根据角色筛选）
//     * @return 分页数据
//     */
//    @Override
//    @Transactional(readOnly = true)
//    @RedisCacheable(
//            key =
//                    "'user:pageUsers:' + #page + ':' + #size + ':' + (#keyword != null ? #keyword : '') + ':' + (#roleId != null ? #roleId : '')",
//            ttl = 1800, // 30分钟缓存
//            type = Page.class,
//            keyPrefix = "springoj:cache:")
//    public Page<AuthUser> pageUsers(int page, int size, String keyword, Long roleId) {
//        keyword = keyword == null ? "" : keyword;
//        Page<AuthUser> userPage = new Page<>(page, size);
//        List<AuthUser> records = userMapper.selectUserPage(userPage, keyword, roleId);
//        userPage.setRecords(records);
//        userPage.getRecords().forEach(u -> u.setRoles(roleMapper.findRolesByUserId(u.getUserId())));
//        return userPage;
//    }
//
//    /**
//     * 保存用户信息
//     *
//     * @param user 用户对象
//     * @return 保存成功返回true，失败抛出异常
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    @RedisEvict(keyPrefix = "springoj:cache:", allEntries = true, keys = "user:pageUsers:")
//    public boolean save(AuthUser user) {
//        // 设置默认值
//        if (user.getStatus() == null) {
//            user.setStatus(1); // 默认激活状态
//        }
//        if (user.getCreateTime() == null) {
//            user.setCreateTime(LocalDateTime.now()); // 设置创建时间
//        }
//
//        // 密码加密处理
//        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
//            user.setPassword(passwordEncoder.encode(user.getPassword()));
//        }
//
//        // 保存用户基本信息
//        if (userMapper.insert(user) <= 0) {
//            throw new RuntimeException("保存失败");
//        }
//
//        // 保存用户角色关联信息
//        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
//            user.getRoles()
//                    .forEach(r -> userRoleMapper.insert(new UserRole(user.getUserId(), r.getId())));
//        }
//        return true;
//    }
//
//    /**
//     * 根据用户ID更新用户信息
//     *
//     * @param user 用户对象，包含要更新的用户信息
//     * @return 更新成功返回true
//     * @throws RuntimeException 当更新失败时抛出异常
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    @RedisEvict(keyPrefix = "springoj:cache:", allEntries = true, keys = "user:pageUsers:")
//    public boolean updateById(AuthUser user) {
//        // 如果密码有更新，则进行加密
//        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
//            user.setPassword(passwordEncoder.encode(user.getPassword()));
//        }
//
//        // 更新用户基本信息，如果更新失败则抛出异常
//        if (userMapper.updateById(user) <= 0) {
//            throw new RuntimeException("更新失败");
//        }
//
//        // 更新用户角色关联信息：先删除原有角色关联，再插入新角色关联
//        userRoleMapper.deleteByUserId(user.getUserId());
//        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
//            user.getRoles()
//                    .forEach(r -> userRoleMapper.insert(new UserRole(user.getUserId(), r.getId())));
//        }
//        return true;
//    }
//}
