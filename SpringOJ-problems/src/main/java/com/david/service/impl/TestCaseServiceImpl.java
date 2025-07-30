package com.david.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.dto.InputDto;
import com.david.judge.TestCase;
import com.david.judge.TestCaseInput;
import com.david.mapper.TestCaseMapper;
import com.david.service.ITestCaseService;
import com.david.vo.TestCaseVo;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 测试用例服务实现类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {

	private final TestCaseInputServiceImpl testCaseInputService;
	private final TestCaseMapper testCaseMapper;

	@Override
	public List<TestCaseVo> getTestCaseVoByProblemId(Long problemId) {
		List<TestCase> testCases = getSampleTestCases(problemId);
		if (testCases.isEmpty()) {
			return Collections.emptyList();
		}

		Map<Long, List<TestCaseInput>> inputsByTestCaseId = getInputsByTestCaseIds(
				testCases.stream().map(TestCase::getId).toList());

		List<TestCaseVo> testCaseVos = testCases.stream().map(testCase -> buildTestCaseVo(testCase, inputsByTestCaseId))
				.toList();
		return testCaseVos;
	}

	@Override
	public List<TestCase> getTestCasesByProblemId(Long problemId) {
		List<TestCase> testCases = lambdaQuery().eq(TestCase::getProblemId, problemId).list();

		if (testCases.isEmpty()) {
			return testCases;
		}

		Map<Long, List<TestCaseInput>> inputsByTestCaseId = getInputsByTestCaseIds(
				testCases.stream().map(TestCase::getId).toList());

		testCases.forEach(testCase -> testCase.setInputs(buildInputDtos(inputsByTestCaseId.get(testCase.getId()))));

		return testCases;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveTestCase(TestCase testCase) {
		// 保存测试用例
		testCaseMapper.insert(testCase);

		// 保存输入数据
		if (!CollectionUtils.isEmpty(testCase.getInputs())) {
			List<TestCaseInput> inputs = buildTestCaseInputs(testCase.getId(), testCase.getInputs());
			testCaseInputService.saveBatch(inputs);
		}

		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateTestCase(TestCase testCase) {
		// 更新测试用例
		updateById(testCase);

		// 删除旧地输入数据并保存新的
		testCaseInputService.lambdaUpdate().eq(TestCaseInput::getTestCaseId, testCase.getId()).remove();

		if (!CollectionUtils.isEmpty(testCase.getInputs())) {
			List<TestCaseInput> inputs = buildTestCaseInputs(testCase.getId(), testCase.getInputs());
			testCaseInputService.saveBatch(inputs);
		}

		return true;
	}

	/**
	 * 获取示例测试用例
	 */
	private List<TestCase> getSampleTestCases(Long problemId) {
		return lambdaQuery().eq(TestCase::getProblemId, problemId).eq(TestCase::getIsSample, true).list();
	}

	/**
	 * 根据测试用例ID列表获取输入数据映射
	 */
	private Map<Long, List<TestCaseInput>> getInputsByTestCaseIds(List<Long> testCaseIds) {
		if (testCaseIds.isEmpty()) {
			return Collections.emptyMap();
		}

		return testCaseInputService.lambdaQuery().in(TestCaseInput::getTestCaseId, testCaseIds).list().stream()
				.collect(Collectors.groupingBy(TestCaseInput::getTestCaseId));
	}

	/**
	 * 构建TestCaseVo对象
	 */
	private TestCaseVo buildTestCaseVo(TestCase testCase, Map<Long, List<TestCaseInput>> inputsByTestCaseId) {
		List<InputDto> inputs = buildInputDtos(inputsByTestCaseId.get(testCase.getId()));
		return TestCaseVo.builder().inputs(inputs).output(testCase.getOutput()).build();
	}

	/**
	 * 构建InputDto列表
	 */
	private List<InputDto> buildInputDtos(List<TestCaseInput> testCaseInputs) {
		if (CollectionUtils.isEmpty(testCaseInputs)) {
			return Collections.emptyList();
		}

		return testCaseInputs.stream().sorted(Comparator.comparing(TestCaseInput::getOrderIndex)).map(
				input -> InputDto.builder().input(input.getInputContent()).inputName(input.getTestCaseName()).build())
				.toList();
	}

	/**
	 * 构建TestCaseInput列表
	 */
	private List<TestCaseInput> buildTestCaseInputs(Long testCaseId, List<InputDto> inputDtos) {
		AtomicInteger index = new AtomicInteger(0);
		return inputDtos.stream()
				.map(input -> TestCaseInput.builder().testCaseId(testCaseId).inputContent(input.getInput())
						.testCaseName(input.getInputName()).orderIndex(index.getAndIncrement()).build())
				.toList();
	}
}