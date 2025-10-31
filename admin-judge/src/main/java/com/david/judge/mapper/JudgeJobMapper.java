package com.david.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.entity.JudgeJob;
import com.david.judge.mapper.model.NodeFinishedAggregate;
import com.david.judge.mapper.model.NodeStatusAggregate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface JudgeJobMapper extends BaseMapper<JudgeJob> {

    @Select({
        "<script>",
        "SELECT node_id, status, COUNT(*) AS count",
        "FROM judge_jobs",
        "WHERE node_id IN",
        "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "GROUP BY node_id, status",
        "</script>"
    })
    List<NodeStatusAggregate> aggregateByNodeIds(@Param("nodeIds") Collection<Long> nodeIds);

    @Select({
        "<script>",
        "SELECT node_id, COUNT(*) AS count",
        "FROM judge_jobs",
        "WHERE node_id IN",
        "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "AND status = 'finished'",
        "AND finished_at &gt;= #{threshold}",
        "GROUP BY node_id",
        "</script>"
    })
    List<NodeFinishedAggregate> aggregateRecentFinished(
            @Param("nodeIds") Collection<Long> nodeIds, @Param("threshold") LocalDateTime threshold);
}
