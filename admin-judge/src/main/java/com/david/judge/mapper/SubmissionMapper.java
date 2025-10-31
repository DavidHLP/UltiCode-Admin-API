package com.david.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.entity.Submission;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    @Select({
        """
        SELECT DISTINCT s.id
        FROM submissions s
        LEFT JOIN users u ON u.id = s.user_id
        LEFT JOIN problems p ON p.id = s.problem_id
        WHERE u.username LIKE CONCAT('%', #{keyword}, '%')
           OR p.slug LIKE CONCAT('%', #{keyword}, '%')
        ORDER BY s.created_at DESC
        LIMIT #{limit}
        """
    })
    List<Long> searchSubmissionIdsByKeyword(
            @Param("keyword") String keyword, @Param("limit") int limit);
}
