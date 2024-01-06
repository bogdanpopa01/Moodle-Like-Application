package com.ing.hubs.model.entity.course.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ing.hubs.model.entity.course.Course;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Slf4j
@Entity
@Builder
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID id;

    @Column(name = "course_type", nullable = false)
    private CourseType courseType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;


    @Column(name = "weekday", nullable = false)
    private Weekday weekday;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public void addCourse(Course course){
        this.course = course;
    }
}