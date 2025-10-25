package com.david.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.entity.SubmissionArtifact;
import com.david.judge.mapper.model.SubmissionArtifactAggregate;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubmissionArtifactMapper extends BaseMapper<SubmissionArtifact> {

    @Select({
        "<script>",
        "SELECT submission_id, COUNT(*) AS count",
        "FROM submission_artifacts",
        "WHERE submission_id IN",
        "<foreach collection='submissionIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "GROUP BY submission_id",
        "</script>"
    })
    List<SubmissionArtifactAggregate> aggregateArtifacts(
            @Param("submissionIds") Collection<Long> submissionIds);
}
