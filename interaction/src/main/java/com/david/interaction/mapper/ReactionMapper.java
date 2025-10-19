package com.david.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.interaction.entity.Reaction;
import com.david.interaction.mapper.result.ReactionAggregationRow;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReactionMapper extends BaseMapper<Reaction> {

    @Select({
        "<script>",
        "SELECT entity_id, kind, COUNT(*) AS total",
        "FROM reactions",
        "WHERE entity_type = #{entityType}",
        "<if test='entityIds != null and entityIds.size() > 0'>",
        "AND entity_id IN",
        "<foreach collection='entityIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</if>",
        "GROUP BY entity_id, kind",
        "</script>"
    })
    List<ReactionAggregationRow> aggregateByEntity(
            @Param("entityType") String entityType,
            @Param("entityIds") Collection<Long> entityIds);
}

