package com.david.dto;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseDto{
    private String input;
    private String output;
}
