package com.ing.hubs.service;

import com.ing.hubs.configuration.Constant;
import com.ing.hubs.dto.user.UserDto;
import com.ing.hubs.dto.user.UserPatchDto;
import com.ing.hubs.dto.user.UserResponseDto;

import com.ing.hubs.exception.DuplicateDataException;
import com.ing.hubs.exception.EntityNotFoundException;
import com.ing.hubs.exception.user.InvalidEmailException;
import com.ing.hubs.exception.CouldNotDeleteEntityException;
import com.ing.hubs.exception.user.InvalidDateOfBirthException;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    private SecurityService securityService;
    private PasswordEncoder passwordEncoder;

    public UserResponseDto createUser(final UserDto dto) {
        dto.setPhoneNumber(dto.getPhoneNumber().replaceAll("\\s", ""));

        this.validateUniqueData(dto.getUsername(), dto.getEmail(), dto.getPhoneNumber());
        this.validateEmail(dto.getEmail());
        this.validateDateOfBirth(dto.getDateOfBirth());


        final User user = modelMapper.map(dto, User.class);

        this.setUserType(user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        final User savedUser = this.saveUser(user);
        log.info(String.format("User with id \"%s\" has been created", savedUser.getId()));

        return this.modelMapper.map(savedUser, UserResponseDto.class);
    }

    public User saveUser(final User user) {
        try {
            return this.userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateDataException(ex);
        }
    }

    public UserResponseDto findById(final UUID id) {
        return this.modelMapper.map(this.findUserById(id), UserResponseDto.class);
    }

    public User findUserById(final UUID id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User"));
    }

    public List<UserResponseDto> findAllUsers(final Role role) {
        return this.userRepository.findAll()
                .stream()
                .filter(user -> role == null || user.getRole().equals(role))
                .map(user -> this.modelMapper.map(user, UserResponseDto.class))
                .toList();
    }

    public void deleteById(final String jwtToken) {
        final UUID id = this.securityService.extractUserIdFromToken(jwtToken);
        final User user = this.findUserById(id);

        final List<Enrollment> teachersEnrollments = user.getCourses()
                .stream()
                .flatMap(course -> course.getEnrollments().stream())
                .toList();

        if (teachersEnrollments.stream()
                .anyMatch(enr ->
                        enr.getStatus() == EnrollmentStatus.APPROVED ||
                        enr.getStatus() == EnrollmentStatus.ACTIVE)) {
            throw new CouldNotDeleteEntityException("Students are enrolled in teachers courses");
        }

        userRepository.deleteById(id);
        log.info(String.format("User with id \"%s\" has been deleted", user.getId()));
    }

    public UserResponseDto updateUser(final String jwtToken,
                                      final UserPatchDto userPatchDto) {
        final User user = this.findUserById(this.securityService.extractUserIdFromToken(jwtToken));

        this.validateUniqueData(null, null, userPatchDto.getPhoneNumber());

        Optional.ofNullable(userPatchDto.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(userPatchDto.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(userPatchDto.getPassword()).ifPresent(password -> {
            user.setPassword(passwordEncoder.encode(userPatchDto.getPassword()));
        });
        Optional.ofNullable(userPatchDto.getPhoneNumber()).ifPresent(user::setPhoneNumber);

        this.saveUser(user);
        log.info(String.format("User with id \"%s\" has been updated", user.getId()));

        return this.modelMapper.map(user, UserResponseDto.class);
    }

    private void validateEmail(final String email) {
        if (!email.matches("^[a-zA-Z]+[.][a-zA-Z0-9]+@(?:stud[.])?poodle[.]com$")) {
            throw new InvalidEmailException();
        }
    }

    private void setUserType(final User user) {
        if (user.getEmail().matches("^[a-zA-Z]+[.][a-zA-Z0-9]+@stud[.]poodle[.]com$")) {
            log.info(String.format("Role has been set to: \"%s\"", Role.STUDENT));
            user.setRole(Role.STUDENT);
        } else {
            log.info(String.format("Role has been set to: \"%s\"", Role.TEACHER));
            user.setRole(Role.TEACHER);
        }
    }

    private void validateUniqueData(final String username,
                                    final String email,
                                    final String phoneNumber) {
        if (this.userRepository.existsByUsername(username)) {
            throw new DuplicateDataException(new DataIntegrityViolationException(Constant.DUPLICATE_USERNAME));
        }
        if (this.userRepository.existsByEmail(email)) {
            throw new DuplicateDataException(new DataIntegrityViolationException(Constant.DUPLICATE_EMAIL));
        }
        if (this.userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateDataException(new DataIntegrityViolationException(Constant.DUPLICATE_PHONE_NUMBER));
        }
    }

    private void validateDateOfBirth(final LocalDate dateOfBirth){
        if (dateOfBirth.isAfter(LocalDate.now().minusYears(10)) || dateOfBirth.isBefore(LocalDate.now().minusYears(100))){
            throw new InvalidDateOfBirthException();
        }
    }
}
