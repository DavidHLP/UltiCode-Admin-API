package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.SolutionCommentMapper;
import com.david.pojo.SolutionComments;
import com.david.service.ISolutionCommentService;
import com.david.vo.SolutionCommentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolutionCommentServiceImpl extends ServiceImpl<SolutionCommentMapper, SolutionComments> implements ISolutionCommentService {

    private final SolutionCommentMapper solutionCommentMapper;

    @Override
    public List<SolutionCommentVo> getCommentsBySolutionId(Long solutionId) {
        List<SolutionCommentVo> voList = solutionCommentMapper.selectCommentsBySolutionId(solutionId);
        return buildCommentTree(voList);
    }

    private List<SolutionCommentVo> buildCommentTree(List<SolutionCommentVo> comments) {
        Map<Long, List<SolutionCommentVo>> childrenMap = comments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(SolutionCommentVo::getParentId));

        comments.forEach(comment -> comment.setChildren(childrenMap.get(comment.getId())));

        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
    }
}

