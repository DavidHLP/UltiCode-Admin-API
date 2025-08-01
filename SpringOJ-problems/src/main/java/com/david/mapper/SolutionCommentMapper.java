package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.pojo.SolutionComments;
import com.david.vo.SolutionCommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SolutionCommentMapper extends BaseMapper<SolutionComments> {
    List<SolutionCommentVo> selectCommentsBySolutionId(@Param("solutionId") Long solutionId);
}
