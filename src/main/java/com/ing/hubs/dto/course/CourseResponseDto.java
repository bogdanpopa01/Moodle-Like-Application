package com.ing.hubs.dto.course;

import com.ing.hubs.dto.schedule.ScheduleDto;
import com.ing.hubs.dto.schedule.ScheduleResponseDto;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponseDto {
    private UUID id;
    private String courseName;
    private String description;
    private Integer capacity;
    private Integer credits;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<ScheduleResponseDto> schedules;
}
