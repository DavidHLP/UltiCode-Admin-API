package com.david.common.forward;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link org.springframework.security.core.Authentication}
 * implementation that represents an identity propagated from the gateway.
 */
public class ForwardedAuthenticationToken extends AbstractAuthenticationToken {

    private final ForwardedUser principal;
    private final String credentials;

    public ForwardedAuthenticationToken(
            ForwardedUser principal, Collection<? extends GrantedAuthority> authorities) {
        this(principal, authorities, "N/A");
    }

    public ForwardedAuthenticationToken(
            ForwardedUser principal,
            Collection<? extends GrantedAuthority> authorities,
            String credentials) {
        super(List.copyOf(authorities));
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public ForwardedUser getPrincipal() {
        return principal;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }
}
