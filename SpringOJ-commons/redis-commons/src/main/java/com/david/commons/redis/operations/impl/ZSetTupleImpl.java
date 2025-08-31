package com.david.commons.redis.operations.impl;

import com.david.commons.redis.operations.RedisZSetOperations.ZSetTuple;

/**
 * ZSet 元素分数对实现类
 *
 * @param <T> 值类型
 * @author David
 */
public class ZSetTupleImpl<T> implements ZSetTuple<T> {

    private final T value;
    private final Double score;

    public ZSetTupleImpl(T value, Double score) {
        this.value = value;
        this.score = score;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public Double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ZSetTupleImpl<?> that = (ZSetTupleImpl<?>) o;

        if (value != null ? !value.equals(that.value) : that.value != null)
            return false;
        return score != null ? score.equals(that.score) : that.score == null;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (score != null ? score.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZSetTupleImpl{" +
                "value=" + value +
                ", score=" + score +
                '}';
    }
}