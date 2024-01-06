package com.ing.hubs.controller;

import com.ing.hubs.dto.course.CourseDto;
import com.ing.hubs.dto.course.CoursePatchDto;
import com.ing.hubs.dto.course.CourseResponseDto;
import com.ing.hubs.service.CourseService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/courses")
public class CourseController {
    private CourseService courseService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CourseResponseDto create(@RequestBody @Valid final CourseDto dto,
                                    @RequestHeader(name = "Authorization") final String jwtToken) {
        return this.courseService.createCourse(dto,jwtToken);
    }

    @GetMapping
    public List<CourseResponseDto> getAll(@RequestParam(required = false, name = "teacherId") final UUID teacherId) {
        return this.courseService.findAllCourses(teacherId);
    }

    @GetMapping("/{id}")
    public CourseResponseDto findById(@PathVariable final UUID id) {
        return this.courseService.findById(id);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable final UUID id,
                       @RequestHeader(name = "Authorization") final String jwtToken) {
        this.courseService.deleteById(id, jwtToken);
    }

    @PatchMapping("/{id}")
    public CourseResponseDto update(@PathVariable final UUID id,
                                    @RequestBody @Valid final CoursePatchDto dto,
                                    @RequestHeader(name = "Authorization") final String jwtToken) {
        return this.courseService.updateCourse(id, dto, jwtToken);
    }

}
