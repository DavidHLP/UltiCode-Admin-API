package com.david.interaction.mapper.result;

import lombok.Data;

@Data
public class ReactionAggregationRow {

    private Long entityId;
    private String kind;
    private Long total;
}

