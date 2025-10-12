package com.david.service.impl;

import com.david.entity.Tag;
import com.david.mapper.TagMapper;
import com.david.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
