package com.david.problem.service;

import static com.david.problem.service.support.CrudSupport.*;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.core.exception.BusinessException;
import com.david.problem.dto.DifficultyCreateRequest;
import com.david.problem.dto.DifficultyUpdateRequest;
import com.david.problem.dto.DifficultyView;
import com.david.problem.dto.PageResult;
import com.david.problem.entity.Difficulty;
import com.david.problem.entity.Problem;
import com.david.problem.mapper.DifficultyMapper;
import com.david.problem.mapper.ProblemMapper;

import jakarta.annotation.Nullable;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class DifficultyManagementService {

    private final DifficultyMapper difficultyMapper;
    private final ProblemMapper problemMapper;

    public DifficultyManagementService(
            DifficultyMapper difficultyMapper, ProblemMapper problemMapper) {
        this.difficultyMapper = difficultyMapper;
        this.problemMapper = problemMapper;
    }

    public PageResult<DifficultyView> listDifficulties(
            int page, int size, @Nullable String keyword) {
        var query = Wrappers.<Difficulty>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            Integer idFilter = tryParseInteger(trimmed);
            query.and(
                    w -> {
                        w.like(Difficulty::getCode, trimmed);
                        if (idFilter != null) {
                            w.or().eq(Difficulty::getId, idFilter);
                        }
                    });
        }
        query.orderByAsc(Difficulty::getSortKey).orderByAsc(Difficulty::getId);

        return selectPageAndMap(difficultyMapper, new Page<>(page, size), query, this::toView);
    }

    public DifficultyView getDifficulty(Integer difficultyId) {
        Difficulty difficulty = requireFound(difficultyMapper.selectById(difficultyId), "难度不存在");
        return toView(difficulty);
    }

    @Transactional
    public DifficultyView createDifficulty(DifficultyCreateRequest request) {
        if (difficultyMapper.selectById(request.id()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "难度ID已存在");
        }
        String code = normalizeRequiredLower(request.code(), "难度编码");
        ensureUnique(
                difficultyMapper, Difficulty::getCode, code, Difficulty::getId, null, "难度编码已存在");

        Difficulty difficulty = new Difficulty();
        difficulty.setId(request.id());
        difficulty.setCode(code);
        difficulty.setSortKey(request.sortKey());
        difficultyMapper.insert(difficulty);
        return getDifficulty(difficulty.getId());
    }

    @Transactional
    public DifficultyView updateDifficulty(Integer difficultyId, DifficultyUpdateRequest request) {
        Difficulty existing = requireFound(difficultyMapper.selectById(difficultyId), "难度不存在");
        boolean changed = false;

        if (request.code() != null) {
            String code = normalizeRequiredLower(request.code(), "难度编码");
            if (!Objects.equals(code, existing.getCode())) {
                ensureUnique(
                        difficultyMapper,
                        Difficulty::getCode,
                        code,
                        Difficulty::getId,
                        difficultyId,
                        "难度编码已存在");
                existing.setCode(code);
                changed = true;
            }
        }
        if (request.sortKey() != null) {
            changed |= setIfChanged(request.sortKey(), existing.getSortKey(), existing::setSortKey);
        }

        if (!changed) {
            return toView(existing);
        }

        updateById(
                difficultyMapper,
                Difficulty::getId,
                difficultyId,
                uw -> {
                    uw.set(Difficulty::getCode, existing.getCode());
                    uw.set(Difficulty::getSortKey, existing.getSortKey());
                });
        return getDifficulty(difficultyId);
    }

    @Transactional
    public void deleteDifficulty(Integer difficultyId) {
        Difficulty existing = requireFound(difficultyMapper.selectById(difficultyId), "难度不存在");
        Long count =
                problemMapper.selectCount(
                        Wrappers.<Problem>lambdaQuery().eq(Problem::getDifficultyId, difficultyId));
        assertNoRelations(count, "仍有题目关联该难度，无法删除");
        difficultyMapper.deleteById(existing.getId());
    }

    private DifficultyView toView(Difficulty difficulty) {
        return new DifficultyView(
                difficulty.getId(), difficulty.getCode(), difficulty.getSortKey());
    }
}
