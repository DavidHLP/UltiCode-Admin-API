package com.david.service.impl;

import com.david.entity.Testcase;
import com.david.mapper.TestcaseMapper;
import com.david.service.TestcaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TestcaseServiceImpl extends ServiceImpl<TestcaseMapper, Testcase> implements TestcaseService {

}
