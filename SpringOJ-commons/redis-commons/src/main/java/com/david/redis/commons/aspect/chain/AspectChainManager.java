package com.david.redis.commons.aspect.chain;

import com.david.log.commons.LogUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
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
                        .sorted(Comparator.comparingInt(AspectHandler::getOrder))
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

}
