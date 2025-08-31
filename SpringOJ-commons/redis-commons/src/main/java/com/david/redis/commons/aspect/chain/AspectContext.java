package com.david.redis.commons.aspect.chain;

import lombok.Getter;
import lombok.Setter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 切面上下文对象
 *
 * <p>
 * 包含切面处理过程中的所有必要信息和状态。
 *
 * @author David
 */
@Getter
@Setter
public class AspectContext {

	/** 连接点 */
	private final ProceedingJoinPoint joinPoint;

	/** 目标方法 */
	private final Method method;

	/** 方法参数 */
	private final Object[] args;

	/** 目标对象 */
	private final Object target;

	/** 切面类型 */
	private final AspectType aspectType;

	/** 上下文属性，用于处理器间传递数据 */
	private final Map<String, Object> attributes = new ConcurrentHashMap<>();
	/** 处理开始时间 */
	private final long startTime = System.currentTimeMillis();
	/** 方法执行结果 */
	private Object result;
	/** 是否已执行原方法 */
	private boolean methodExecuted = false;

	/** 异常信息 */
	private Throwable exception;

	public AspectContext(ProceedingJoinPoint joinPoint, AspectType aspectType) {
		this.joinPoint = joinPoint;
		this.aspectType = aspectType;
		this.method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		this.args = joinPoint.getArgs();
		this.target = joinPoint.getTarget();
	}

	/**
	 * 获取属性值
	 *
	 * @param key  属性键
	 * @param type 属性类型
	 * @param <T>  属性值类型
	 * @return 属性值，如果不存在或类型不匹配则返回 null
	 */
	public <T> T getAttribute(String key, Class<T> type) {
		Object value = attributes.get(key);
		return type.isInstance(value) ? type.cast(value) : null;
	}

	/**
	 * 获取属性值，如果不存在则返回默认值
	 *
	 * @param key          属性键
	 * @param type         属性类型
	 * @param defaultValue 默认值
	 * @param <T>          属性值类型
	 * @return 属性值或默认值
	 */
	public <T> T getAttribute(String key, Class<T> type, T defaultValue) {
		Object value = attributes.get(key);
		return type.isInstance(value) ? type.cast(value) : defaultValue;
	}

	/**
	 * 设置属性值
	 *
	 * @param key   属性键
	 * @param value 属性值（为 null 时移除该属性）
	 */
	public void setAttribute(String key, Object value) {
		if (key == null) {
			throw new IllegalArgumentException("Attribute key cannot be null");
		}
		if (value == null) {
			attributes.remove(key);
		} else {
			attributes.put(key, value);
		}
	}

	/**
	 * 移除属性
	 *
	 * @param key 属性键
	 * @return 被移除的属性值
	 */
	public Object removeAttribute(String key) {
		return attributes.remove(key);
	}

	/**
	 * 检查是否包含指定属性
	 *
	 * @param key 属性键
	 * @return true 如果包含该属性
	 */
	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	/**
	 * 获取方法签名字符串
	 *
	 * @return 方法签名
	 */
	public String getMethodSignature() {
		if (method == null) {
			return "UnknownClass.unknownMethod";
		}
		return method.getDeclaringClass().getSimpleName() + "." + method.getName();
	}

	/**
	 * 获取执行耗时
	 *
	 * @return 执行耗时（毫秒）
	 */
	public long getExecutionTime() {
		return System.currentTimeMillis() - startTime;
	}

	/**
	 * 是否有异常
	 *
	 * @return true 如果有异常
	 */
	public boolean hasException() {
		return exception != null;
	}

	/**
	 * 执行原方法
	 *
	 * @throws Throwable 方法执行异常
	 */
	public void proceedMethod() throws Throwable {
		if (joinPoint == null) {
			throw new IllegalStateException("Cannot proceed with null joinPoint (dummy context)");
		}

		if (methodExecuted) {
			throw new IllegalStateException("Method has already been executed");
		}

		try {
			result = joinPoint.proceed();
			methodExecuted = true;
		} catch (Throwable throwable) {
			exception = throwable;
			methodExecuted = true;
			throw throwable;
		}
	}
}
