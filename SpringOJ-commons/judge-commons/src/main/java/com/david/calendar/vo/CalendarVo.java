package com.david.calendar.vo;


import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarVo {
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;
	private Integer count;
}
