package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.user.AuthUser;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author david
 * @since 2025-07-20
 */
@Mapper
public interface UserMapper extends BaseMapper<AuthUser> {

    /**
     * 分页查询用户（按关键字匹配用户名或邮箱）
     *
     * @param page 第几页（从1开始）及分页大小
     * @param keyword 可选关键字（用户名/邮箱 模糊匹配）
     * @return 当前页的用户列表
     */
    List<AuthUser> selectUserPage(
            Page<AuthUser> page,
            @Param("keyword") String keyword,
            @Param("roleId") Long roleId,
            @Param("status") Boolean status);
}
