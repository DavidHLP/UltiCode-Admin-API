package com.david.solution;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpDownCounts {
	private Long targetId;
	/** 点赞数 */
	private Integer upvotes;

	/** 点踩数 */
	private Integer downvotes;
}
