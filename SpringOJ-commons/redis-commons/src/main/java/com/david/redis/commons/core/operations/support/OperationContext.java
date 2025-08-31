package com.david.redis.commons.core.operations.support;

import lombok.Builder;
import lombok.experimental.Accessors;

/**
 * Redis操作上下文
 *
 * <p>
 * 封装Redis操作的上下文信息，包括操作类型、键名、参数、以及期待的返回类型等。
 *
 * @author David
 * @param operation   操作类型（如 GET, SET, HGET 等）
 * @param key         Redis键名
 * @param params      操作参数
 * @param returnType  期待的返回类型
 * @param description 操作描述（用于日志）
 * @param <T>         参数的类型
 * @param <R>         返回值的类型
 */
@Builder
@Accessors(fluent = true)
public record OperationContext<T, R>(
		String operation, String key, T params, Class<R> returnType, String description) {

	/**
	 * 创建无参数的操作上下文（指定返回类型）
	 *
	 * @param operation  操作类型
	 * @param key        Redis键名
	 * @param returnType 期待的返回类型
	 * @param <R>        返回值的类型
	 * @return OperationContext实例
	 */
	public static <R> OperationContext<Void, R> ofNoParams(String operation, String key, Class<R> returnType) {
		return OperationContext.<Void, R>builder()
				.operation(operation)
				.key(key)
				.returnType(returnType)
				.description(RedisOperationType.fromCommand(operation).getDescription())
				.build();
	}

	/**
	 * 创建带单个参数的操作上下文（指定返回类型）
	 *
	 * @param operation  操作类型
	 * @param key        Redis键名
	 * @param param      参数
	 * @param returnType 期待的返回类型
	 * @param <T>        参数类型
	 * @param <R>        返回值类型
	 * @return OperationContext实例
	 */
	public static <T, R> OperationContext<T, R> of(String operation, String key, T param, Class<R> returnType) {
		return OperationContext.<T, R>builder()
				.operation(operation)
				.key(key)
				.params(param)
				.returnType(returnType)
				.description(RedisOperationType.fromCommand(operation).getDescription())
				.build();
	}

	/**
	 * 使用操作类型枚举创建无参数的操作上下文
	 *
	 * @param operationType 操作类型枚举
	 * @param key           Redis键名
	 * @param returnType    期待的返回类型
	 * @param <R>           返回值的类型
	 * @return OperationContext实例
	 */
	public static <R> OperationContext<Void, R> of(RedisOperationType operationType, String key, Class<R> returnType) {
		return OperationContext.<Void, R>builder()
				.operation(operationType.getCommand())
				.key(key)
				.returnType(returnType)
				.description(operationType.getDescription())
				.build();
	}

	/**
	 * 使用操作类型枚举创建带参数的操作上下文
	 *
	 * @param operationType 操作类型枚举
	 * @param key           Redis键名
	 * @param param         参数
	 * @param returnType    期待的返回类型
	 * @param <T>           参数类型
	 * @param <R>           返回值类型
	 * @return OperationContext实例
	 */
	public static <T, R> OperationContext<T, R> of(RedisOperationType operationType, String key, T param,
			Class<R> returnType) {
		return OperationContext.<T, R>builder()
				.operation(operationType.getCommand())
				.key(key)
				.params(param)
				.returnType(returnType)
				.description(operationType.getDescription())
				.build();
	}

	/**
	 * 创建自定义描述的操作上下文
	 *
	 * @param operation   操作类型
	 * @param key         Redis键名
	 * @param param       参数
	 * @param returnType  期待的返回类型
	 * @param description 自定义描述
	 * @param <T>         参数类型
	 * @param <R>         返回值类型
	 * @return OperationContext实例
	 */
	public static <T, R> OperationContext<T, R> withDescription(
			String operation, String key, T param, Class<R> returnType, String description) {
		return OperationContext.<T, R>builder()
				.operation(operation)
				.key(key)
				.params(param)
				.returnType(returnType)
				.description(description)
				.build();
	}

	/**
	 * 获取操作类型枚举
	 *
	 * @return 操作类型枚举
	 */
	public RedisOperationType getOperationType() {
		return RedisOperationType.fromCommand(operation);
	}

	/**
	 * 检查是否有参数
	 *
	 * @return 如果有参数返回true，否则返回false
	 */
	public boolean hasParams() {
		return params != null;
	}

	/**
	 * 检查是否指定了返回类型
	 *
	 * @return 如果指定了返回类型返回true，否则返回false
	 */
	public boolean hasReturnType() {
		return returnType != null;
	}

	/**
	 * 获取格式化的操作描述（用于日志）
	 *
	 * @return 格式化的操作描述
	 */
	public String getFormattedDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(description != null ? description : "Redis操作");
		sb.append(" [").append(operation).append("]");
		if (key != null) {
			sb.append(" 键: ").append(key);
		}
		if (hasParams()) {
			sb.append(" 参数: ").append(params);
		}
		return sb.toString();
	}
}