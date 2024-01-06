package com.ing.hubs.model.entity.course;

import com.ing.hubs.model.entity.course.schedule.CourseType;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void shouldAddSchedulesToCourse(){
        final Set<Schedule> schedules = Set.of(
                Schedule.builder()
                        .courseType(CourseType.SEMINAR)
                        .build(),
                Schedule.builder()
                        .courseType(CourseType.LAB)
                        .build(),
                Schedule.builder()
                        .courseType(CourseType.COURSE)
                        .build()
        );

        final Course course = new Course();

        course.addSchedules(schedules);

        assertEquals(course.getSchedules(), schedules);
        schedules.forEach(schedule ->
                assertEquals(schedule.getCourse(), course)
        );
     }

}