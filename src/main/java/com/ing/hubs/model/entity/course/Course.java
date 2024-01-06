package com.ing.hubs.model.entity.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ing.hubs.model.entity.course.schedule.Schedule;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Entity
@Builder
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID id;

    @Column(name = "course_name", nullable = false, unique = true)
    private String courseName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "credits", nullable = false)
    private Integer credits;


    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;


    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private Set<Enrollment> enrollments = new LinkedHashSet<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Schedule> schedules = new LinkedHashSet<>();

    public void addSchedules(final Set<Schedule> schedules){
        this.schedules = schedules;
        schedules.forEach(schedule -> schedule.addCourse(this));
    }

}