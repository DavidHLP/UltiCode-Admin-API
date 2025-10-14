package com.david.auth.support;

import java.time.Instant;

public record JwtToken(String token, Instant expiresAt) {
}
