package com.david.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.interaction.dto.PageResult;
import com.david.interaction.dto.ReactionDeleteRequest;
import com.david.interaction.dto.ReactionQuery;
import com.david.interaction.dto.ReactionView;
import com.david.interaction.entity.Reaction;
import com.david.interaction.mapper.ReactionMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReactionAdminService {

    private final ReactionMapper reactionMapper;

    public ReactionAdminService(ReactionMapper reactionMapper) {
        this.reactionMapper = reactionMapper;
    }

    public PageResult<ReactionView> listReactions(ReactionQuery query) {
        int page = query.page() == null || query.page() < 1 ? 1 : query.page();
        int size = query.size() == null || query.size() < 1 ? 10 : query.size();
        Page<Reaction> pager = new Page<>(page, size);

        LambdaQueryWrapper<Reaction> wrapper = Wrappers.lambdaQuery(Reaction.class);
        if (query.userId() != null) {
            wrapper.eq(Reaction::getUserId, query.userId());
        }
        if (StringUtils.hasText(query.entityType())) {
            wrapper.eq(Reaction::getEntityType, query.entityType().trim());
        }
        if (query.entityId() != null) {
            wrapper.eq(Reaction::getEntityId, query.entityId());
        }
        if (StringUtils.hasText(query.kind())) {
            wrapper.eq(Reaction::getKind, query.kind().trim());
        }
        if (StringUtils.hasText(query.source())) {
            wrapper.eq(Reaction::getSource, query.source().trim());
        }
        wrapper.orderByDesc(Reaction::getCreatedAt);

        Page<Reaction> result = reactionMapper.selectPage(pager, wrapper);
        List<ReactionView> items =
                result.getRecords().stream().map(this::toView).toList();
        return new PageResult<>(
                items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public void deleteReaction(ReactionDeleteRequest request) {
        reactionMapper.delete(
                Wrappers.lambdaQuery(Reaction.class)
                        .eq(Reaction::getUserId, request.userId())
                        .eq(Reaction::getEntityType, request.entityType())
                        .eq(Reaction::getEntityId, request.entityId())
                        .eq(Reaction::getKind, request.kind()));
    }

    private ReactionView toView(Reaction reaction) {
        return new ReactionView(
                reaction.getUserId(),
                reaction.getEntityType(),
                reaction.getEntityId(),
                reaction.getKind(),
                reaction.getWeight(),
                reaction.getSource(),
                reaction.getMetadata(),
                reaction.getCreatedAt(),
                reaction.getUpdatedAt());
    }
}

