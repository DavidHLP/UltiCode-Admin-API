package com.david.testcase;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 测试用例实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("test_cases")
public class TestCase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 测试用例ID，主键，自动增长
     */
    private Long id;

    /**
     * 关联的题目ID，外键
     */
    private Long problemId;

	/**
	 * 该测试用例的输出
	 */
	private TestCaseOutput testCaseOutput;

	/**
	 * 该测试用例的输入集合
	 */
	private List<TestCaseInput> testCaseInput;
}
