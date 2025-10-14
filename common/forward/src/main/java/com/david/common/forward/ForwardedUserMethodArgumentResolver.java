package com.david.common.forward;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Allows {@link ForwardedUser} to be injected into MVC controllers. */
public class ForwardedUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean annotated = parameter.hasParameterAnnotation(CurrentForwardedUser.class);
        boolean typeMatches = ForwardedUser.class.isAssignableFrom(parameter.getParameterType());
        return annotated && typeMatches;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ForwardedAuthenticationToken token) {
            return token.getPrincipal();
        }
        return null;
    }
}
