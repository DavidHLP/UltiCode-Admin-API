package com.david.strategy;

import com.david.chain.utils.JudgmentContext;
import com.david.enums.LanguageType;
import com.david.strategy.impl.JavaSandboxStrategy;
import com.david.strategy.utils.StrategyContext;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码运行策略工厂类
 *
 * <p>负责管理不同编程语言的执行策略，提供统一的策略获取接口。 使用策略模式 + 工厂模式，支持动态扩展新的编程语言。
 *
 * <p>支持的功能：
 *
 * <ul>
 *   <li>策略注册与管理
 *   <li>策略缓存优化
 *   <li>语言支持检查
 *   <li>异常处理与日志记录
 * </ul>
 *
 * @author David
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeRunFactory {

    private final JavaSandboxStrategy javaSandboxStrategy;

    // 预留其他语言策略的注入位置
    // private final PythonSandboxStrategy pythonSandboxStrategy;
    // private final CppSandboxStrategy cppSandboxStrategy;

    /** 策略实例映射表，使用EnumMap提高性能 */
    private final Map<LanguageType, Strategy> strategyMap = new EnumMap<>(LanguageType.class);

    /** 策略上下文缓存，避免重复创建对象 */
    private final Map<LanguageType, StrategyContext> contextCache = new ConcurrentHashMap<>();

    /**
     * 初始化所有可用的代码执行策略
     *
     * <p>在Spring容器启动时自动调用，注册所有已实现的语言策略。 未来添加新语言时，只需要在此方法中添加相应的注册调用。
     */
    @PostConstruct
    public void initStrategies() {

        // 注册已实现的策略
        registerStrategy(LanguageType.JAVA, javaSandboxStrategy);

        // 预留其他语言策略注册位置
        // registerStrategy(LanguageType.PYTHON, pythonSandboxStrategy);
        // registerStrategy(LanguageType.CPP, cppSandboxStrategy);
        // registerStrategy(LanguageType.C, cSandboxStrategy);
        // registerStrategy(LanguageType.JAVASCRIPT, jsSandboxStrategy);

        log.info("策略工厂初始化完成，已注册 {} 种语言策略: {}", strategyMap.size(), strategyMap.keySet());

        // 预热缓存
        preloadContextCache();
    }

    /**
     * 注册策略到工厂
     *
     * @param languageType 语言类型，不能为null
     * @param strategy 对应的策略实现，不能为null
     * @throws IllegalArgumentException 当参数为null时抛出
     */
    private void registerStrategy(LanguageType languageType, Strategy strategy) {
        if (languageType == null) {
            throw new IllegalArgumentException("语言类型不能为null");
        }
        if (strategy == null) {
            log.warn("尝试注册null策略，语言类型: {}", languageType);
            return;
        }

        strategyMap.put(languageType, strategy);
        log.debug("成功注册策略: {} -> {}", languageType, strategy.getClass().getSimpleName());
    }

    /** 预加载策略上下文缓存 在应用启动时预先创建所有策略上下文，提高运行时性能 */
    private void preloadContextCache() {
        log.debug("开始预加载策略上下文缓存...");
        strategyMap.forEach(
                (languageType, strategy) ->
                        contextCache.put(languageType, new StrategyContext(strategy)));
        log.debug("策略上下文缓存预加载完成，缓存数量: {}", contextCache.size());
    }

    /**
     * 获取指定语言的策略上下文
     *
     * <p>该方法会先从缓存中查找，如果缓存未命中则创建新的上下文并缓存。 使用缓存机制可以避免重复创建对象，提高性能。
     *
     * @param language 编程语言类型，不能为null
     * @return 对应的策略上下文，永不为null
     * @throws IllegalArgumentException 当语言为null或不支持时抛出
     */
    public StrategyContext getStrategy(LanguageType language) {
        if (language == null) {
            log.warn("尝试获取策略时传入了null语言类型");
            throw new IllegalArgumentException("语言类型不能为null");
        }

        // 检查策略是否存在
        Strategy strategy = strategyMap.get(language);
        if (strategy == null) {
            String supportedLanguages = strategyMap.keySet().toString();
            log.warn("尝试获取不支持的语言策略: {}, 当前支持的语言: {}", language, supportedLanguages);
            throw new IllegalArgumentException(
                    String.format("暂不支持的语言: %s，当前支持的语言: %s", language, supportedLanguages));
        }

        // 先从缓存中获取，未命中则创建
        StrategyContext context = contextCache.computeIfAbsent(language, lt -> {
            StrategyContext ctx = new StrategyContext(strategyMap.get(lt));
            log.debug("为语言 {} 创建并缓存了新的策略上下文", lt);
            return ctx;
        });

        return context;
    }

    /**
     * 直接执行指定语言的策略
     *
     * <p>这是对外的便捷方法：内部将根据语言类型获取或创建对应的 {@link StrategyContext}，并调用其 {@code execute} 方法。
     *
     * @param language 编程语言类型，不能为空
     * @param request  评测上下文，包含题目、提交、代码与测试用例等信息
     * @return 是否评测通过
     * @throws IllegalArgumentException 当语言不受支持或为null时抛出
     */
    public Boolean execute(LanguageType language, JudgmentContext request) {
        StrategyContext context = getStrategy(language);
        return context.execute(request);
    }

    /**
     * 检查是否支持指定编程语言
     *
     * @param language 编程语言类型
     * @return true表示支持，false表示不支持
     */
    public boolean isLanguageSupported(LanguageType language) {
        return language != null && strategyMap.containsKey(language);
    }

    /**
     * 获取所有支持的编程语言类型
     *
     * @return 支持的语言类型集合（不可修改）
     */
    public Set<LanguageType> getSupportedLanguages() {
        return Collections.unmodifiableSet(strategyMap.keySet());
    }

    /**
     * 获取已注册策略的数量
     *
     * @return 策略数量
     */
    public int getStrategyCount() {
        return strategyMap.size();
    }

    /**
     * 清空策略缓存
     *
     * <p>主要用于测试或在运行时重新加载策略时调用。 生产环境中一般不需要调用此方法。
     */
    public void clearCache() {
        contextCache.clear();
        log.info("策略上下文缓存已清空");
    }
}
