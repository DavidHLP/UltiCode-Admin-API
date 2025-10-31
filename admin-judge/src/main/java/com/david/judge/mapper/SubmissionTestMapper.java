package com.david.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.entity.SubmissionTest;
import com.david.judge.mapper.model.SubmissionTestAggregate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SubmissionTestMapper extends BaseMapper<SubmissionTest> {

    @Select({
        "<script>",
        "SELECT submission_id, COUNT(*) AS total,",
        "SUM(CASE WHEN verdict = 'AC' THEN 1 ELSE 0 END) AS passed",
        "FROM submission_tests",
        "WHERE submission_id IN",
        "<foreach collection='submissionIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "GROUP BY submission_id",
        "</script>"
    })
    List<SubmissionTestAggregate> aggregateTests(
            @Param("submissionIds") Collection<Long> submissionIds);
}
