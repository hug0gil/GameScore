package com.gamescore.back.model.DTOs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {
    private List<String> labels;
    private List<Long> dataPoints;
}