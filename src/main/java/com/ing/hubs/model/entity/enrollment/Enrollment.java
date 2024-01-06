package com.ing.hubs.model.entity.enrollment;

import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Entity
@Builder
@Table(name = "enrollment")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID id;

    @Column(name = "grade")
    @Min(value = 1)
    @Max(value = 10)
    private Integer grade;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

}