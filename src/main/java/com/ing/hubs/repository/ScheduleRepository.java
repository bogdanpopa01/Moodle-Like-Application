package com.ing.hubs.repository;

import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    Set<Schedule> findSchedulesByCourse(Course course);

}
