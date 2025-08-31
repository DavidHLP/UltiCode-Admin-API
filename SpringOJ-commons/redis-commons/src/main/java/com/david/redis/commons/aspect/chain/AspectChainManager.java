package com.david.redis.commons.aspect.chain;

import com.david.log.commons.core.LogUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 切面处理器链管理器
 *
 * <p>负责管理和创建不同类型的处理器链。
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class AspectChainManager {

    private final LogUtils logUtils;
    private final List<AspectHandler> allHandlers;

    /**
     * 为指定切面类型创建处理器链
     *
     * @param aspectType 切面类型
     * @return 处理器链
     */
    public AspectChain createChain(AspectType aspectType) {
        List<AspectHandler> typeHandlers =
                allHandlers.stream()
                        .filter(handler -> handler.supports(aspectType))
                        .sorted((h1, h2) -> Integer.compare(h1.getOrder(), h2.getOrder()))
                        .collect(Collectors.toList());

        logUtils.business()
                .trace(
                        "aspect_chain_manager",
                        "create_chain",
                        "success",
                        "aspectType: " + aspectType,
                        "handlerCount: " + typeHandlers.size(),
                        "handlers: "
                                + typeHandlers.stream()
                                        .map(AspectHandler::getName)
                                        .collect(Collectors.joining(", ")));

        return AspectChain.create(typeHandlers, logUtils);
    }

    /**
     * 获取所有注册的处理器
     *
     * @return 按类型分组的处理器映射
     */
    public Map<AspectType, List<AspectHandler>> getAllHandlers() {
        return allHandlers.stream()
                .collect(
                        Collectors.groupingBy(
                                handler -> {
                                    // 获取处理器支持的第一个类型作为分组键
                                    Set<AspectType> supportedTypes =
                                            ((AbstractAspectHandler) handler)
                                                    .getSupportedAspectTypes();
                                    return supportedTypes.isEmpty()
                                            ? AspectType.GENERAL
                                            : supportedTypes.iterator().next();
                                }));
    }
}
