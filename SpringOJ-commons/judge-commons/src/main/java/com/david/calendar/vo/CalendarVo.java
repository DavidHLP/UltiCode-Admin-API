package com.david.calendar.vo;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarVo {
	@JSONField(format = "yyyy-MM-dd")
	private LocalDateTime date;
	private Integer count;
}
