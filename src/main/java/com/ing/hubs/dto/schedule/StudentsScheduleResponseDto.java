package com.ing.hubs.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentsScheduleResponseDto {
    private String courseName;
    private List<ScheduleResponseDto> schedules;
}
