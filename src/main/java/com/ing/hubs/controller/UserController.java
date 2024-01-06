package com.ing.hubs.controller;

import com.ing.hubs.dto.enrollment.EnrollmentResponseDto;
import com.ing.hubs.dto.jwt.JwtDto;
import com.ing.hubs.dto.schedule.StudentsScheduleResponseDto;
import com.ing.hubs.dto.user.UserDto;
import com.ing.hubs.dto.user.UserPatchDto;
import com.ing.hubs.dto.user.UserResponseDto;
import com.ing.hubs.dto.user.UserSessionDto;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.service.enrollment.EnrollmentService;
import com.ing.hubs.service.SecurityService;
import com.ing.hubs.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {
    private UserService userService;
    private EnrollmentService enrollmentService;
    private SecurityService securityService;

    @PostMapping("/sessions")
    public JwtDto createSession(@RequestBody @Valid final UserSessionDto dto) {
        return this.securityService.createSession(dto);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserResponseDto createUser(@RequestBody @Valid final UserDto dto) {
        return this.userService.createUser(dto);
    }

    @GetMapping
    public List<UserResponseDto> findAll(@RequestParam(required = false, name = "type") final Role role) {
        return this.userService.findAllUsers(role);
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable final UUID id) {
        return this.userService.findById(id);
    }

    @DeleteMapping("/me")
    public void delete(@RequestHeader(name = "Authorization") final String jwtToken) {
        this.userService.deleteById(jwtToken);
    }

    @PatchMapping("/me")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto update(@RequestBody @Valid final UserPatchDto dto,
                                  @RequestHeader(name = "Authorization") final String jwtToken) {
        return this.userService.updateUser(jwtToken, dto);
    }

    @GetMapping("/me/enrollments")
    public List<EnrollmentResponseDto> getAll(@RequestParam(required = false, name = "status") final EnrollmentStatus enrollmentStatus,
                                              @RequestHeader(name = "Authorization") final String jwtToken) {
        return this.enrollmentService.findAll(enrollmentStatus, jwtToken);
    }

    @GetMapping("/me/grades")
    public Map<String, Integer> viewGrades(@RequestHeader(name = "Authorization") final String jwtToken) {
        return this.enrollmentService.viewGrades(jwtToken);
    }

    @GetMapping("/me/schedules")
    public List<StudentsScheduleResponseDto> findAllStudentsSchedules(@RequestHeader(name = "Authorization") final String jwtToken){
        return this.enrollmentService.findStudentsActiveSchedules(jwtToken);
    }
}
