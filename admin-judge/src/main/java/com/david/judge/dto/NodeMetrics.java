package com.david.judge.dto;

public record NodeMetrics(long queuedJobs, long runningJobs, long failedJobs, long finishedLastHour) {}
