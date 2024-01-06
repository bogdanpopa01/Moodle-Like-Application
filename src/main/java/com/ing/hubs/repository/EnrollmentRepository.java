package com.ing.hubs.repository;

import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findEnrollmentByUserAndCourse(final User user, final Course course);
}
