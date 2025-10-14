package com.david.auth.dto;

import java.util.List;

public record TokenIntrospectResponse(Long userId, String username, List<String> roles) {
}
