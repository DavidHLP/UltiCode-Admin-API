package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.entity.user.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    /**
     * 根据用户ID删除其所有角色关联
     * @param userId 用户ID
     * @return 受影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
}
