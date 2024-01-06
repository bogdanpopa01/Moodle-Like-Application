package com.ing.hubs.model.entity.user;

import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class UserTest {
    @Test
    void shouldAddEnrollmentToUser(){
        final User user = new User();
        final Course course = new Course();
        final Enrollment enrollment = Enrollment.builder().course(course).build();

        user.addEnrollment(enrollment);

        assertTrue(user.getEnrollments().contains(enrollment));
        assertEquals(enrollment.getCourse(), course);
    }

    @Test
    void shouldAddCourseToUser(){
        final User user = new User();
        final Course course = new Course();

        user.addCourse(course);
        assertTrue(user.getCourses().contains(course));
        assertEquals(course.getUser(), user);
    }
}