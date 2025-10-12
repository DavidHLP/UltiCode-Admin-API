package com.david.service.impl;

import com.david.entity.Problem;
import com.david.mapper.ProblemMapper;
import com.david.service.ProblemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

}
