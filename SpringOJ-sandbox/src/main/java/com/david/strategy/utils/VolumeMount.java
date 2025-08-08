package com.david.strategy.utils;

import lombok.Builder;
import lombok.Data;

/**
 * 卷挂载配置
 */
@Data
@Builder
public class VolumeMount {
	private String hostPath;
	private String containerPath;
	private boolean readOnly;
	private String type; // bind, volume, tmpfs
}
