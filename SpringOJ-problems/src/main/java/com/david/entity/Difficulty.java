package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("difficulties")
public class Difficulty {
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id; // 1/2/3

    private String code; // EASY/MEDIUM/HARD
    private Integer sortKey; // 1/2/3
}
