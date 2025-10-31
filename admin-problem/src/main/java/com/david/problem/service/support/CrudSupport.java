package com.david.problem.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.core.exception.BusinessException;
import com.david.problem.dto.PageResult;

import jakarta.annotation.Nullable;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class CrudSupport {

    private CrudSupport() {}

    /** 去首尾空格并校验非空，空则抛 400 */
    public static String normalizeRequired(String value, String fieldLabelZh) {
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldLabelZh + "不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldLabelZh + "不能为空");
        }
        return trimmed;
    }

    /** 同上，并转为小写 */
    public static String normalizeRequiredLower(String value, String fieldLabelZh) {
        return normalizeRequired(value, fieldLabelZh).toLowerCase(Locale.ROOT);
    }

    /** 找不到则抛 404 */
    public static <T> T requireFound(@Nullable T entity, String notFoundMsgZh) {
        if (entity == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, notFoundMsgZh);
        }
        return entity;
    }

    /** 关系计数>0 则抛 409，常用于删除前的关联检查 */
    public static void assertNoRelations(@Nullable Long count, String conflictMsgZh) {
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, conflictMsgZh);
        }
    }

    /** 仅当新旧值不同才设置，并返回是否发生变化（可减少 changed 变量样板代码） */
    public static <T> boolean setIfChanged(T newVal, T oldVal, Consumer<T> setter) {
        if (!Objects.equals(newVal, oldVal)) {
            setter.accept(newVal);
            return true;
        }
        return false;
    }

    /** 字符串转 Integer，失败返回 null（给搜索 id 之类用） */
    public static Integer tryParseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** 通用唯一性校验：在某列 equals 指定值，且（可选）排除某个 id 时，若已存在记录则抛 409。 */
    public static <E> void ensureUnique(
            BaseMapper<E> mapper,
            SFunction<E, ?> uniqueColumn,
            Object value,
            @Nullable SFunction<E, ?> idColumnToExclude,
            @Nullable Object excludeIdValue,
            String conflictMsgZh) {

        LambdaQueryWrapper<E> qw = new LambdaQueryWrapper<E>().eq(uniqueColumn, value);
        if (idColumnToExclude != null && excludeIdValue != null) {
            qw.ne(idColumnToExclude, excludeIdValue);
        }
        Long count = mapper.selectCount(qw);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, conflictMsgZh);
        }
    }

    /** 统一的分页查询并映射为 PageResult。 */
    public static <E, V> PageResult<V> selectPageAndMap(
            BaseMapper<E> mapper,
            Page<E> page,
            LambdaQueryWrapper<E> query,
            Function<E, V> mapperFn) {

        Page<E> result = mapper.selectPage(page, query);
        List<E> records = result.getRecords();
        List<V> items =
                (records == null || records.isEmpty())
                        ? List.of()
                        : records.stream().map(mapperFn).toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 通用的按 id 更新（基于 LambdaUpdateWrapper）。 传入 id 列（例如 Entity::getId）、id 值、以及对 wrapper 的 set 操作。 */
    public static <E> void updateById(
            BaseMapper<E> mapper,
            SFunction<E, ?> idColumn,
            Serializable idValue,
            Consumer<LambdaUpdateWrapper<E>> sets) {

        LambdaUpdateWrapper<E> uw = new LambdaUpdateWrapper<E>().eq(idColumn, idValue);
        sets.accept(uw);
        mapper.update(null, uw);
    }
}
