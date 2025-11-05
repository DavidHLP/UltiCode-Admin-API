package com.david.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.interaction.dto.PageResult;
import com.david.interaction.dto.ReactionDeleteRequest;
import com.david.interaction.dto.ReactionQuery;
import com.david.interaction.dto.ReactionView;
import com.david.interaction.entity.Reaction;
import com.david.interaction.entity.SensitiveWord;
import com.david.interaction.mapper.ReactionMapper;
import com.david.interaction.service.model.SensitiveWordAnalysisResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ReactionAdminService {

    private final ReactionMapper reactionMapper;
    private final SensitiveWordAdminService sensitiveWordAdminService;

    public ReactionAdminService(
            ReactionMapper reactionMapper, SensitiveWordAdminService sensitiveWordAdminService) {
        this.reactionMapper = reactionMapper;
        this.sensitiveWordAdminService = sensitiveWordAdminService;
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
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword().trim();
            wrapper.and(
                    w ->
                            w.like(Reaction::getEntityType, keyword)
                                    .or()
                                    .like(Reaction::getKind, keyword)
                                    .or()
                                    .like(Reaction::getSource, keyword)
                                    .or()
                                    .like(Reaction::getMetadata, keyword));
        }
        wrapper.orderByDesc(Reaction::getCreatedAt);

        Page<Reaction> result = reactionMapper.selectPage(pager, wrapper);
        List<SensitiveWord> activeWords = sensitiveWordAdminService.loadActiveWords();
        List<ReactionView> items =
                result.getRecords().stream()
                        .map(reaction -> toView(reaction, activeWords))
                        .toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
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

    private ReactionView toView(Reaction reaction, List<SensitiveWord> activeWords) {
        SensitiveWordAnalysisResult analysis =
                sensitiveWordAdminService.analyzeContent(reaction.getMetadata(), activeWords);
        boolean hasSensitive = analysis.hasSensitive();
        return new ReactionView(
                reaction.getUserId(),
                reaction.getEntityType(),
                reaction.getEntityId(),
                reaction.getKind(),
                reaction.getWeight(),
                reaction.getSource(),
                reaction.getMetadata(),
                reaction.getCreatedAt(),
                reaction.getUpdatedAt(),
                hasSensitive,
                hasSensitive ? analysis.hits() : List.of(),
                hasSensitive ? analysis.riskLevel() : null);
    }
}
