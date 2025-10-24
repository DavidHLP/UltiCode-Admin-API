package com.david.contest.dto;

import java.util.List;

public record ContestOptionsResponse(
        List<ContestKindOption> kinds,
        List<String> statuses,
        List<String> registrationModes) {}
