package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.aspect.chain.utils.CacheKeyGenerator;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.enums.EvictTiming;
import com.david.redis.commons.properties.RedisCommonsProperties;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 缓存驱逐处理器
 *
 * <p>
 * 负责执行缓存驱逐操作，支持精确键和模式键驱逐。
 *
 * @author David
 */
@Component
public class CacheEvictionHandler extends AbstractAspectHandler {

	private static final String EVICT_PROCESSED_PREFIX = "evict_processed_";
	private static final String DELETED_SUFFIX_PREFIX = ":__DELETED__:";
	private static final int MAX_DISPLAY_KEYS = 10;
	private static final int PREVIEW_KEYS_COUNT = 5;

	private final RedisUtils redisUtils;
	private final CacheKeyGenerator keyGenerator;
	private final RedisCommonsProperties properties;

	/**
	 * 构造函数，初始化缓存驱逐处理器所需的依赖组件
	 *
	 * @param logUtils 日志工具类，用于记录操作日志和异常信息
	 * @param redisUtils Redis工具类，提供Redis操作的封装方法
	 * @param keyGenerator 缓存键生成器，用于生成和解析缓存键
	 * @param properties Redis公共配置属性
	 */
	public CacheEvictionHandler(
			LogUtils logUtils,
			RedisUtils redisUtils,
			CacheKeyGenerator keyGenerator,
			RedisCommonsProperties properties) {
		super(logUtils);
		this.redisUtils = redisUtils;
		this.keyGenerator = keyGenerator;
		this.properties = properties;
	}

	/**
	 * 获取当前处理器支持的切面类型集合
	 *
	 * @return 返回包含CACHE_EVICT类型的集合，表示此处理器专门处理缓存驱逐操作
	 */
	@Override
	protected Set<AspectType> getSupportedAspectTypes() {
		return Set.of(AspectType.CACHE_EVICT);
	}

	/**
	 * 获取处理器在切面链中的执行顺序
	 *
	 * @return 返回30作为基础顺序，实际顺序会根据beforeInvocation属性动态调整
	 */
	@Override
	public int getOrder() {
		return 30; // 基础顺序，会根据beforeInvocation动态调整
	}

	/**
	 * 判断当前处理器是否能够处理给定的切面上下文
	 * 检查是否满足以下条件：
	 * 1. 父类canHandle返回true
	 * 2. 存在RedisEvict注解
	 * 3. 条件表达式满足
	 * 4. 驱逐操作尚未处理过
	 *
	 * @param context 切面执行上下文，包含方法信息、参数等
	 * @return true表示可以处理，false表示不能处理
	 */
	@Override
	public boolean canHandle(AspectContext context) {
		return super.canHandle(context)
				&& getRedisEvictAnnotation(context).isPresent()
				&& isConditionMet(context)
				&& !isEvictProcessed(context);
	}

	/**
	 * 处理缓存驱逐逻辑的主入口方法
	 * 根据注解的beforeInvocation属性决定是在方法执行前还是执行后进行缓存驱逐
	 *
	 * @param context 切面执行上下文
	 * @param chain 切面处理链，用于继续执行下一个处理器
	 * @return 方法执行的结果
	 * @throws Throwable 可能抛出的任何异常
	 */
	@Override
	public Object handle(AspectContext context, AspectChain chain) throws Throwable {
		var annotation = getRedisEvictAnnotation(context)
				.orElseThrow(() -> new IllegalStateException("RedisEvict annotation not found"));

		boolean beforeInvocation = annotation.beforeInvocation();
		String evictProcessedKey = EVICT_PROCESSED_PREFIX + beforeInvocation;

		try {
			if (beforeInvocation) {
				return handleBeforeInvocation(context, chain, evictProcessedKey);
			} else {
				return handleAfterInvocation(context, chain, evictProcessedKey);
			}
		} catch (Exception e) {
			return handleEvictionException(context, chain, e, beforeInvocation, evictProcessedKey);
		}
	}

	/**
	 * 处理方法执行前的缓存驱逐操作
	 * 先执行缓存驱逐，然后标记为已处理，最后继续执行切面链
	 *
	 * @param context 切面执行上下文
	 * @param chain 切面处理链
	 * @param evictProcessedKey 用于标记驱逐操作已处理的键
	 * @return 方法执行结果
	 * @throws Throwable 可能抛出的异常
	 */
	private Object handleBeforeInvocation(AspectContext context, AspectChain chain, String evictProcessedKey) throws Throwable {
		evictCache(context);
		context.setAttribute(evictProcessedKey, true);
		return chain.proceed(context);
	}

	/**
	 * 处理方法执行后的缓存驱逐操作
	 * 先执行切面链中的方法，如果方法执行成功则进行缓存驱逐并标记为已处理
	 *
	 * @param context 切面执行上下文
	 * @param chain 切面处理链
	 * @param evictProcessedKey 用于标记驱逐操作已处理的键
	 * @return 方法执行结果
	 * @throws Throwable 可能抛出的异常
	 */
	private Object handleAfterInvocation(AspectContext context, AspectChain chain, String evictProcessedKey) throws Throwable {
		Object result = chain.proceed(context);

		if (context.isMethodExecuted()) {
			evictCache(context);
			context.setAttribute(evictProcessedKey, true);
		}

		return result;
	}

	/**
	 * 处理缓存驱逐过程中发生的异常
	 * 记录异常日志，如果是方法执行前的驱逐失败，标记为已处理以避免重复处理
	 *
	 * @param context 切面执行上下文
	 * @param chain 切面处理链
	 * @param e 发生的异常
	 * @param beforeInvocation 是否为方法执行前的驱逐
	 * @param evictProcessedKey 驱逐处理标记键
	 * @return 方法执行结果
	 * @throws Throwable 继续抛出异常
	 */
	private Object handleEvictionException(AspectContext context, AspectChain chain, Exception e,
	                                       boolean beforeInvocation, String evictProcessedKey) throws Throwable {
		logException(context, "cache_evict", e, "缓存驱逐失败");

		if (beforeInvocation) {
			context.setAttribute(evictProcessedKey, true);
		}

		return chain.proceed(context);
	}

	/**
	 * 执行缓存驱逐操作的核心方法
	 * 如果是虚拟上下文（method为null），则跳过驱逐操作
	 * 根据RedisEvict注解配置执行相应的驱逐策略
	 *
	 * @param context 切面执行上下文
	 */
	private void evictCache(AspectContext context) {
		if (context.getMethod() == null) {
			return; // 虚拟上下文不驱逐缓存
		}

		getRedisEvictAnnotation(context)
				.ifPresent(annotation -> processEvictionByTiming(context, annotation));
	}

	/**
	 * 根据驱逐时机配置处理不同类型的缓存驱逐策略
	 * 支持延迟驱逐、级联驱逐和立即驱逐三种模式
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解实例，包含驱逐配置信息
	 */
	private void processEvictionByTiming(AspectContext context, RedisEvict annotation) {
		EvictTiming timing = annotation.timing();
		long delayMs = annotation.delayMs();

		if (timing == EvictTiming.DELAYED || delayMs > 0) {
			scheduleDelayedEviction(context, annotation, Math.max(delayMs, 0));
		} else if (timing == EvictTiming.CASCADE || annotation.cascade()) {
			performCascadeEviction(context, annotation);
		} else {
			performImmediateEviction(context, annotation);
		}
	}

	/**
	 * 执行立即缓存驱逐操作
	 * 根据softDelete配置决定是执行软删除还是硬删除
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解，包含驱逐配置
	 */
	private void performImmediateEviction(AspectContext context, RedisEvict annotation) {
		if (annotation.softDelete()) {
			performSoftDelete(context, annotation);
		} else {
			performHardDelete(context, annotation);
		}
	}

	/**
	 * 执行硬删除操作，直接从Redis中删除缓存数据
	 * 根据allEntries配置决定是删除所有匹配条目还是指定键
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置
	 */
	private void performHardDelete(AspectContext context, RedisEvict annotation) {
		if (annotation.allEntries()) {
			evictAllEntries(context, annotation);
		} else {
			evictSpecificKeys(context, annotation);
		}
	}

	/**
	 * 执行软删除操作，不直接删除数据而是重命名键添加删除标记
	 * 软删除的数据可以在需要时恢复，适用于需要保留数据备份的场景
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置
	 */
	private void performSoftDelete(AspectContext context, RedisEvict annotation) {
		String softDeleteSuffix = DELETED_SUFFIX_PREFIX + System.currentTimeMillis();

		if (annotation.allEntries()) {
			performSoftDeleteAll(context, annotation, softDeleteSuffix);
		} else {
			performSoftDeleteSpecific(context, annotation, softDeleteSuffix);
		}
	}

	/**
	 * 对所有匹配的缓存条目执行软删除操作
	 * 使用模式匹配找到所有相关的键，然后逐个进行软删除处理
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置
	 * @param suffix 软删除时添加到键名的后缀，包含时间戳
	 */
	private void performSoftDeleteAll(AspectContext context, RedisEvict annotation, String suffix) {
		String keyPrefix = getKeyPrefix(annotation);
		String pattern = keyPrefix + "*";

		try {
			Set<String> keys = getKeysFromPattern(pattern);
			int softDeleteCount = keys.stream()
					.filter(Predicate.not(this::isDeletedKey))
					.mapToInt(key -> processSoftDeleteKey(key, suffix) ? 1 : 0)
					.sum();

			logExecution(context, "cache_soft_delete_all",
					"软删除模式: %s, 删除数量: %d".formatted(pattern, softDeleteCount));
		} catch (Exception e) {
			logException(context, "soft_delete_all", e, "软删除模式失败: " + pattern);
			throw e;
		}
	}

	/**
	 * 对指定的缓存键执行软删除操作
	 * 根据keys配置生成要软删除的键列表，然后逐个处理
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置
	 * @param suffix 软删除后缀标识
	 */
	private void performSoftDeleteSpecific(AspectContext context, RedisEvict annotation, String suffix) {
		List<String> keysToSoftDelete = generateKeysToEvict(context, annotation, annotation.keys());

		int softDeleteCount = keysToSoftDelete.stream()
				.filter(Predicate.not(this::isDeletedKey))
				.mapToInt(key -> processSoftDeleteKey(key, suffix) ? 1 : 0)
				.sum();

		logExecution(context, "cache_soft_delete_keys",
				"软删除键数: %d, 删除数量: %d".formatted(keysToSoftDelete.size(), softDeleteCount));
	}

	/**
	 * 处理单个键的软删除操作
	 * 读取原键的值，创建新地带删除标记的键，然后删除原键
	 *
	 * @param key 要软删除的原始键
	 * @param suffix 软删除标记后缀
	 * @return true表示软删除成功，false表示失败（如键不存在）
	 */
	private boolean processSoftDeleteKey(String key, String suffix) {
		try {
			return Optional.ofNullable(redisUtils.strings().getString(key))
					.map(value -> {
						String newKey = key + suffix;
						redisUtils.strings().set(newKey, value);
						redisUtils.strings().delete(key);
						return true;
					})
					.orElse(false);
		} catch (Exception e) {
			// 单个键的软删除失败不抛出异常，只返回false
			return false;
		}
	}

	/**
	 * 判断给定的键是否为已被软删除的键
	 * 通过检查键名是否包含删除标记后缀来判断
	 *
	 * @param key 要检查的键名
	 * @return true表示是已删除的键，false表示正常键
	 */
	private boolean isDeletedKey(String key) {
		return key.contains(DELETED_SUFFIX_PREFIX);
	}

	/**
	 * 调度延迟执行的缓存驱逐操作
	 * 使用CompletableFuture的延迟执行器在指定时间后异步执行驱逐操作
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置
	 * @param delayMs 延迟时间（毫秒），如果小于等于0则立即执行
	 */
	private void scheduleDelayedEviction(AspectContext context, RedisEvict annotation, long delayMs) {
		if (delayMs <= 0) {
			performImmediateEviction(context, annotation);
			return;
		}

		CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
				.execute(() -> {
					try {
						performImmediateEviction(context, annotation);
					} catch (Exception e) {
						logException(context, "delayed_evict", e,
								"延迟驱逐失败: %dms".formatted(delayMs));
					}
				});
	}

	/**
	 * 执行级联缓存驱逐操作
	 * 先执行基本的驱逐操作，然后根据cascadePatterns配置执行额外的级联删除
	 * 适用于需要同时删除相关联缓存数据的场景
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置，包含级联模式配置
	 */
	private void performCascadeEviction(AspectContext context, RedisEvict annotation) {
		// 首先执行基本驱逐
		performImmediateEviction(context, annotation);

		// 然后执行级联删除
		String[] cascadePatterns = annotation.cascadePatterns();
		if (cascadePatterns.length > 0) {
			String keyPrefix = getKeyPrefix(annotation);

			for (String pattern : cascadePatterns) {
				processCascadePattern(context, pattern, keyPrefix);
			}
		}
	}

	/**
	 * 处理单个级联模式的缓存删除
	 * 解析SpEL表达式生成具体的模式，然后执行模式匹配删除
	 *
	 * @param context 切面执行上下文
	 * @param pattern 级联模式表达式，可能包含SpEL语法
	 * @param keyPrefix 键前缀
	 */
	private void processCascadePattern(AspectContext context, String pattern, String keyPrefix) {
		try {
			String resolvedPattern = keyGenerator.resolveSpELExpression(
					pattern, context.getMethod(), context.getArgs());
			String fullPattern = keyPrefix + resolvedPattern;

			deleteByPattern(context, fullPattern);
		} catch (Exception e) {
			logException(context, "cascade_evict", e, "级联驱逐失败: " + pattern);
		}
	}

	/**
	 * 驱逐所有匹配指定模式的缓存条目
	 * 使用通配符模式找到所有相关的键，然后批量删除
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置
	 */
	private void evictAllEntries(AspectContext context, RedisEvict annotation) {
		String keyPrefix = getKeyPrefix(annotation);
		String pattern = keyPrefix + "*";

		try {
			Set<String> keys = getKeysFromPattern(pattern);

			if (keys.isEmpty()) {
				logExecution(context, "cache_evict_all", "模式驱逐: %s, 无匹配的key".formatted(pattern));
				return;
			}

			Long deletedCount = redisUtils.strings().delete(keys.toArray(String[]::new));
			String keysList = formatKeysList(keys);

			logExecution(context, "cache_evict_all",
					"模式驱逐: %s, 删除数量: %d, 被删除的key: %s".formatted(pattern, deletedCount, keysList));
		} catch (Exception e) {
			logException(context, "cache_evict_all", e, "模式驱逐失败: " + pattern);
			throw e;
		}
	}

	/**
	 * 驱逐注解中指定的特定缓存键
	 * 根据keys配置生成具体的键列表，然后批量删除这些键
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置，包含要删除的键表达式
	 */
	private void evictSpecificKeys(AspectContext context, RedisEvict annotation) {
		String[] keyExpressions = annotation.keys();
		if (keyExpressions.length == 0) {
			return;
		}

		List<String> keysToEvict = generateKeysToEvict(context, annotation, keyExpressions);
		if (keysToEvict.isEmpty()) {
			return;
		}

		int totalDeleted = executeEviction(context, keysToEvict);

		logExecution(context, "cache_evict_keys",
				"键驱逐: [%s], 删除数量: %d".formatted(String.join(", ", keysToEvict), totalDeleted));
	}

	/**
	 * 根据键表达式生成要驱逐的具体键列表
	 * 遍历所有键表达式，使用SpEL解析器生成实际的缓存键
	 *
	 * @param context 切面执行上下文
	 * @param annotation RedisEvict注解配置
	 * @param keyExpressions 键表达式数组，可能包含SpEL语法
	 * @return 生成的具体键列表
	 */
	private List<String> generateKeysToEvict(
			AspectContext context, RedisEvict annotation, String[] keyExpressions) {
		String keyPrefix = getKeyPrefix(annotation);

		return Stream.of(keyExpressions)
				.map(keyExpression -> generateSingleKey(context, keyExpression, keyPrefix))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	/**
	 * 生成单个缓存键
	 * 使用CacheKeyGenerator解析键表达式，处理其中的SpEL语法和参数替换
	 *
	 * @param context 切面执行上下文
	 * @param keyExpression 键表达式，可能包含SpEL语法
	 * @param keyPrefix 键前缀
	 * @return Optional包装的生成结果，如果生成失败则为空
	 */
	private Optional<String> generateSingleKey(AspectContext context, String keyExpression, String keyPrefix) {
		try {
			String generatedKey = keyGenerator.generateKey(
					keyExpression, context.getMethod(), context.getArgs());
			return Optional.of(keyPrefix + generatedKey);
		} catch (Exception e) {
			logException(context, "evict_key_gen", e, "驱逐键生成失败: " + keyExpression);
			return Optional.empty();
		}
	}

	/**
	 * 执行实际的缓存驱逐删除操作
	 * 将键列表按精确键和模式键分组，分别进行批量删除和模式匹配删除
	 *
	 * @param context 切面执行上下文
	 * @param keysToEvict 要删除的键列表
	 * @return 实际删除的键数量总计
	 */
	private int executeEviction(AspectContext context, List<String> keysToEvict) {
		var keyGroups = keysToEvict.stream()
				.collect(Collectors.groupingBy(this::isPatternKey));

		List<String> exactKeys = keyGroups.getOrDefault(false, List.of());
		List<String> patternKeys = keyGroups.getOrDefault(true, List.of());

		int totalDeleted = 0;

		// 批量删除精确键
		if (!exactKeys.isEmpty()) {
			Long exactDeleted = redisUtils.strings().delete(exactKeys.toArray(String[]::new));
			totalDeleted += Optional.ofNullable(exactDeleted).orElse(0L).intValue();
		}

		// 处理模式键
		totalDeleted += patternKeys.stream()
				.mapToInt(pattern -> Optional.ofNullable(deleteByPattern(context, pattern))
						.orElse(0L).intValue())
				.sum();

		return totalDeleted;
	}

	/**
	 * 判断给定的键是否为模式键（包含通配符）
	 * 检查键中是否包含*或?等通配符字符
	 *
	 * @param key 要检查的键
	 * @return true表示是模式键，false表示是精确键
	 */
	private boolean isPatternKey(String key) {
		return key.contains("*") || key.contains("?");
	}

	/**
	 * 根据模式删除匹配的缓存键
	 * 使用模式匹配找到所有符合条件的键，然后批量删除
	 *
	 * @param context 切面执行上下文
	 * @param pattern 匹配模式，可包含通配符
	 * @return 删除的键数量，如果操作失败则返回0
	 */
	private Long deleteByPattern(AspectContext context, String pattern) {
		try {
			Set<String> matchedKeys = getKeysFromPattern(pattern);

			if (matchedKeys.isEmpty()) {
				return 0L;
			}

			return redisUtils.strings().delete(matchedKeys.toArray(String[]::new));
		} catch (Exception e) {
			logException(context, "pattern_evict", e, "模式驱逐失败: " + pattern);
			return 0L;
		}
	}

	// 辅助方法

	/**
	 * 从切面上下文中获取RedisEvict注解实例
	 *
	 * @param context 切面执行上下文
	 * @return Optional包装的RedisEvict注解，如果不存在则为空
	 */
	private Optional<RedisEvict> getRedisEvictAnnotation(AspectContext context) {
		return Optional.ofNullable(context.getMethod())
				.map(method -> method.getAnnotation(RedisEvict.class));
	}

	/**
	 * 检查驱逐条件是否满足
	 * 从上下文中获取条件处理器设置的条件满足标记
	 *
	 * @param context 切面执行上下文
	 * @return true表示条件满足，false表示条件不满足
	 */
	private boolean isConditionMet(AspectContext context) {
		return context.getAttribute(CacheConditionHandler.EVICT_CONDITION_MET_ATTR, true);
	}

	/**
	 * 检查驱逐操作是否已经处理过
	 * 根据beforeInvocation属性检查相应的处理标记，避免重复执行驱逐操作
	 *
	 * @param context 切面执行上下文
	 * @return true表示已处理，false表示未处理
	 */
	private boolean isEvictProcessed(AspectContext context) {
		return getRedisEvictAnnotation(context)
				.map(annotation -> {
					String evictProcessedKey = EVICT_PROCESSED_PREFIX + annotation.beforeInvocation();
					return context.getAttribute(evictProcessedKey, false);
				})
				.orElse(false);
	}

	/**
	 * 根据模式获取匹配的Redis键集合
	 * 优先使用scan命令避免阻塞，如果scan结果为空则回退到keys命令
	 *
	 * @param pattern 匹配模式
	 * @return 匹配的键集合
	 */
	private Set<String> getKeysFromPattern(String pattern) {
		Set<String> keys = redisUtils.strings().scanKeys(pattern);
		if (keys.isEmpty()) {
			keys = redisUtils.strings().keys(pattern);
		}
		return keys;
	}

	/**
	 * 格式化键列表用于日志输出
	 * 当键数量较少时显示完整列表，数量较多时只显示前几个并标注总数
	 *
	 * @param keys 要格式化的键集合
	 * @return 格式化后的字符串表示
	 */
	private String formatKeysList(Set<String> keys) {
		if (keys.size() <= MAX_DISPLAY_KEYS) {
			return "[%s]".formatted(String.join(", ", keys));
		} else {
			String preview = keys.stream()
					.limit(PREVIEW_KEYS_COUNT)
					.collect(Collectors.joining(", "));
			return "[%d个key: %s...]".formatted(keys.size(), preview);
		}
	}

	/**
	 * 获取缓存键前缀
	 * 优先使用注解中指定的keyPrefix，如果未设置则使用全局配置的前缀
	 *
	 * @param annotation RedisEvict注解实例
	 * @return 缓存键前缀字符串
	 */
	private String getKeyPrefix(RedisEvict annotation) {
		return StringUtils.hasText(annotation.keyPrefix())
				? annotation.keyPrefix()
				: properties.getCache().getKeyPrefix();
	}
}