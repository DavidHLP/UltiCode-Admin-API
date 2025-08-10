package com.david.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.role.Role;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
	List<Role> findRolesByUserId(@Param("userId") Long userId);
	List<Role> selectRolePage(Page<Role> rolePage, @Param("keyword") String keyword, @Param("status") Integer status);
}
