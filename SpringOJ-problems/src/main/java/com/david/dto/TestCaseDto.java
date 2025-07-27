package com.david.dto;
import com.david.judge.TestCase;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestCaseDto extends TestCase {
    private String input;
    private String output;
}
