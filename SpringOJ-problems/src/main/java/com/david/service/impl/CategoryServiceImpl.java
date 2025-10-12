package com.david.service.impl;

import com.david.entity.Category;
import com.david.mapper.CategoryMapper;
import com.david.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
