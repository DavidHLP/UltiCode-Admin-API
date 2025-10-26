package com.david.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.problem.dto.DifficultyCreateRequest;
import com.david.problem.dto.DifficultyUpdateRequest;
import com.david.problem.dto.DifficultyView;
import com.david.problem.dto.PageResult;
import com.david.problem.entity.Difficulty;
import com.david.problem.entity.Problem;
import com.david.core.exception.BusinessException;
import com.david.problem.mapper.DifficultyMapper;
import com.david.problem.mapper.ProblemMapper;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        LambdaQueryWrapper<Difficulty> query = Wrappers.lambdaQuery(Difficulty.class);
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            Integer idFilter = tryParseInteger(trimmed);
            query.and(
                    wrapper -> {
                        wrapper.like(Difficulty::getCode, trimmed);
                        if (idFilter != null) {
                            wrapper.or().eq(Difficulty::getId, idFilter);
                        }
                    });
        }
        query.orderByAsc(Difficulty::getSortKey).orderByAsc(Difficulty::getId);
        Page<Difficulty> pager = new Page<>(page, size);
        Page<Difficulty> result = difficultyMapper.selectPage(pager, query);
        List<Difficulty> difficulties = result.getRecords();
        List<DifficultyView> items = difficulties == null || difficulties.isEmpty()
                ? List.of()
                : difficulties.stream().map(this::toView).toList();
        return new PageResult<>(
                items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public DifficultyView getDifficulty(Integer difficultyId) {
        Difficulty difficulty = difficultyMapper.selectById(difficultyId);
        if (difficulty == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "难度不存在");
        }
        return toView(difficulty);
    }

    @Transactional
    public DifficultyView createDifficulty(DifficultyCreateRequest request) {
        if (difficultyMapper.selectById(request.id()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "难度ID已存在");
        }
        String code = normalizeCode(request.code());
        ensureCodeUnique(code, null);

        Difficulty difficulty = new Difficulty();
        difficulty.setId(request.id());
        difficulty.setCode(code);
        difficulty.setSortKey(request.sortKey());
        difficultyMapper.insert(difficulty);
        return getDifficulty(difficulty.getId());
    }

    @Transactional
    public DifficultyView updateDifficulty(Integer difficultyId, DifficultyUpdateRequest request) {
        Difficulty existing = difficultyMapper.selectById(difficultyId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "难度不存在");
        }
        boolean changed = false;

        if (request.code() != null) {
            String code = normalizeCode(request.code());
            if (!Objects.equals(code, existing.getCode())) {
                ensureCodeUnique(code, difficultyId);
                existing.setCode(code);
                changed = true;
            }
        }
        if (request.sortKey() != null && !Objects.equals(request.sortKey(), existing.getSortKey())) {
            existing.setSortKey(request.sortKey());
            changed = true;
        }
        if (!changed) {
            return toView(existing);
        }
        LambdaUpdateWrapper<Difficulty> update = Wrappers.lambdaUpdate(Difficulty.class).eq(Difficulty::getId,
                difficultyId);
        update.set(Difficulty::getCode, existing.getCode());
        update.set(Difficulty::getSortKey, existing.getSortKey());
        difficultyMapper.update(null, update);
        return getDifficulty(difficultyId);
    }

    @Transactional
    public void deleteDifficulty(Integer difficultyId) {
        Difficulty existing = difficultyMapper.selectById(difficultyId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "难度不存在");
        }
        Long count = problemMapper.selectCount(
                Wrappers.lambdaQuery(Problem.class)
                        .eq(Problem::getDifficultyId, difficultyId));
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "仍有题目关联该难度，无法删除");
        }
        difficultyMapper.deleteById(difficultyId);
    }

    private void ensureCodeUnique(String code, @Nullable Integer excludeDifficultyId) {
        LambdaQueryWrapper<Difficulty> query = Wrappers.lambdaQuery(Difficulty.class).eq(Difficulty::getCode, code);
        if (excludeDifficultyId != null) {
            query.ne(Difficulty::getId, excludeDifficultyId);
        }
        Long count = difficultyMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "难度编码已存在");
        }
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "难度编码不能为空");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private Integer tryParseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private DifficultyView toView(Difficulty difficulty) {
        return new DifficultyView(difficulty.getId(), difficulty.getCode(), difficulty.getSortKey());
    }
}
