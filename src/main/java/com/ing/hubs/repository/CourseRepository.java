package com.ing.hubs.repository;

import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByUser(final User user);
    boolean existsByCourseName(final String courseName);
    Optional<Course> findByCourseName(final String courseName);
}
