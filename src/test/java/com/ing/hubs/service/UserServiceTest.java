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
import com.ing.hubs.model.entity.course.Course;
import com.ing.hubs.model.entity.enrollment.Enrollment;
import com.ing.hubs.model.entity.enrollment.EnrollmentStatus;
import com.ing.hubs.model.entity.user.Gender;
import com.ing.hubs.model.entity.user.Role;
import com.ing.hubs.model.entity.user.User;
import com.ing.hubs.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Spy
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityService securityService;
    @InjectMocks
    private UserService userService;


    @Nested
    class CreateUserTests {
        private UserDto userDto;
        private User user;

        @BeforeEach
        void setup() {
            this.userDto = new UserDto("firstName",
                    "lastName",
                    Gender.MALE,
                    LocalDate.now().minusYears(15),
                    "username",
                    "password",
                    "firstName.lastName@poodle.com",
                    "+40 123 456 789");

            this.user = User.builder()
                    .firstName(this.userDto.getFirstName())
                    .lastName(this.userDto.getLastName())
                    .gender(this.userDto.getGender())
                    .dateOfBirth(this.userDto.getDateOfBirth())
                    .username(this.userDto.getUsername())
                    .password(this.userDto.getPassword())
                    .email(this.userDto.getEmail())
                    .phoneNumber(this.userDto.getPhoneNumber().replaceAll(" ", ""))
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(user);
            when(passwordEncoder.encode(anyString())).thenReturn(anyString());
        }

        @Test
        void shouldSaveUserWhenDataIsValid() {
            final UserResponseDto responseDto = userService.createUser(userDto);

            assertEquals(userDto.getFirstName(), responseDto.getFirstName());
            assertEquals(userDto.getLastName(), responseDto.getLastName());
            assertEquals(userDto.getGender(), responseDto.getGender());
            assertEquals(userDto.getDateOfBirth(), responseDto.getDateOfBirth());
            assertEquals(userDto.getUsername(), responseDto.getUsername());
            assertEquals(userDto.getEmail(), responseDto.getEmail());
            assertEquals(userDto.getPhoneNumber().replaceAll(" ", ""), responseDto.getPhoneNumber());
        }

        @Test
        void shouldEncodePasscode() {
            userService.createUser(userDto);

            verify(passwordEncoder, times(1)).encode(anyString());
        }

        @Test
        void shouldMapUserDtoToUserAndUserToResponseDto() {
            final UserResponseDto responseDto = userService.createUser(userDto);

            verify(modelMapper, times(1)).map(any(User.class), eq(UserResponseDto.class));
            verify(modelMapper, times(1)).map(any(UserDto.class), eq(User.class));
            assertEquals(UserResponseDto.class, userService.createUser(this.userDto).getClass());
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/SetRoleFromEmailData.csv")
        void shouldSetUserTypeBasedOnEmail(final String email,
                                           final String role,
                                           final CapturedOutput capturedOutput) {
            final UserDto dto = this.userDto;
            dto.setEmail(email);

            userService.createUser(dto);
            assertThat(capturedOutput.getOut(), containsString(role));
        }
    }

    @Test
    void shouldCallUserRepositoryWhenSavingUser() {
        userService.saveUser(new User());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Nested
    class DuplicateOrInvalidDataValidationTests {
        private UserDto userDto;

        @BeforeEach
        void setup() {
            this.userDto = new UserDto();
            userDto.setPhoneNumber("phoneNumber");
            userDto.setEmail("email");
            userDto.setUsername("username");
            userDto.setDateOfBirth(LocalDate.now());
        }

        @Test
        void shouldNotSaveUserWhenUsernameIsDuplicate() {
            when(userRepository.existsByUsername(anyString())).thenReturn(true);

            DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> userService.createUser(this.userDto));
            verify(userRepository, never()).save(any());
            assertThat(exception.getMessage(), containsString("The username you entered already exists"));
        }

        @Test
        void shouldNotSaveUserWhenPhoneNumberIsDuplicate() {
            when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);

            DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> userService.createUser(this.userDto));
            verify(userRepository, never()).save(any());
            assertThat(exception.getMessage(), containsString("The phone number you entered already exists"));
        }

        @Test
        void shouldNotSaveUserWhenEmailIsDuplicate() {
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> userService.createUser(this.userDto));
            verify(userRepository, never()).save(any());
            assertThat(exception.getMessage(), containsString("The email you entered already exists"));
        }

        @Test
        void shouldThrowInvalidDateOfBirthExWhenUserIsUnderage() {
            this.userDto.setEmail("firstName.lastName@poodle.com");

            InvalidDateOfBirthException exception = assertThrows(InvalidDateOfBirthException.class, () -> userService.createUser(this.userDto));
            assertThat(exception.getMessage(), containsString("Enter a valid date of birth"));
        }
    }

    @Nested
    class TeacherDeletionTests {
        private final String jwtToken = "validToken";
        private User user = User
                .builder()
                .id(UUID.randomUUID())
                .role(Role.TEACHER)
                .build();

        private Course course = Course
                .builder()
                .courseName("Java")
                .build();

        @Test
        void shouldNotDeleteTeacherWhenStudentsEnrolledInCourse1() {
            Enrollment enrollment = Enrollment
                    .builder()
                    .status(EnrollmentStatus.ACTIVE)
                    .build();

            course.setEnrollments(new HashSet<>() {{
                add(enrollment);
            }});

            user.addCourse(course);

            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(user.getId());
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            CouldNotDeleteEntityException exception = assertThrows(CouldNotDeleteEntityException.class, () -> userService.deleteById(jwtToken));
            assertThat(exception.getMessage(), containsString("Students are enrolled in teachers courses"));
        }

        @Test
        void shouldNotDeleteTeacherWhenStudentsEnrolledInCourse() {
            final Enrollment enrollment = Enrollment
                    .builder()
                    .status(EnrollmentStatus.APPROVED)
                    .build();

            course.setEnrollments(new HashSet<>() {{
                add(enrollment);
            }});

            user.addCourse(course);

            when(securityService.extractUserIdFromToken(jwtToken)).thenReturn(user.getId());
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            CouldNotDeleteEntityException exception = assertThrows(CouldNotDeleteEntityException.class, () -> userService.deleteById(jwtToken));
            assertThat(exception.getMessage(), containsString("Students are enrolled in teachers courses"));
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/InvalidEmailTestData.csv", delimiter = '\n')
    void shouldThrowInvalidEmailExceptionWhenEmailIsNotValid(String email) {
        final UserDto dto = new UserDto("firstName",
                "lastName",
                Gender.MALE,
                LocalDate.now(),
                "username",
                "password",
                email,
                "+40 123 456 789");

        Assertions.assertThrows(InvalidEmailException.class, () -> this.userService.createUser(dto));
    }

    @Test
    void shouldThrowDuplicateDataExceptionWhenDataIntegrityViolationExceptionIsThrown() {
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException(Constant.DUPLICATE_EMAIL));

        DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> this.userService.saveUser(new User()));
        assertThat(exception.getMessage(),containsString("The email you entered already exists"));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionUserNotInDb() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> this.userService.findUserById(id));
        assertThat(exception.getMessage(),containsString("User not found!"));
    }

    @Test
    void shouldReturnUserResponseDtoWhenFindingById() {
        final UUID id = UUID.randomUUID();
        final User user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.now())
                .username("username")
                .password("password")
                .email("firstName.lastName@poodle.com")
                .phoneNumber("+40123456789")
                .build();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        userService.findById(id);
        userService.findUserById(id);

        verify(userRepository, times(2)).findById(id);
        assertEquals(User.class, this.userService.findUserById(UUID.randomUUID()).getClass());
        assertEquals(UserResponseDto.class, this.userService.findById(UUID.randomUUID()).getClass());
        assertEquals(user.getFirstName(), this.userService.findById(UUID.randomUUID()).getFirstName());
    }

    @Test
    void shouldReturnAllUsersWhenArgumentIsNull() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        final List<UserResponseDto> result = userService.findAllUsers(null);

        verify(userRepository, times(1)).findAll();
        assertEquals(2, result.size());
    }

    @Test
    void shouldFilterUsersWhenArgumentIsNotNull() {
        final User user1 = User.builder()
                .role(Role.TEACHER)
                .build();
        final User user2 = User.builder()
                .role(Role.STUDENT)
                .build();

        final UserResponseDto responseDto1 = new UserResponseDto();
        final UserResponseDto responseDto2 = new UserResponseDto();

        responseDto1.setRole(Role.TEACHER);
        responseDto2.setRole(Role.STUDENT);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        final List<UserResponseDto> result = userService.findAllUsers(Role.TEACHER);

        assertEquals(1, result.size());
    }

    @Test
    void shouldDeleteUserWhenUserIsInDb() {
        final UUID id = UUID.randomUUID();

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));

        userService.deleteById(anyString());

        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldExtractIdFromJwtWhenUpdatingUser() {
        when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(new User()));

        userService.updateUser(anyString(), new UserPatchDto());

        verify(securityService, times(1)).extractUserIdFromToken(anyString());
    }

    @Test
    void shouldNotSaveUserWhenDuplicateDataExceptionIsThrown() {
        final UserPatchDto patchDto = new UserPatchDto();
        patchDto.setPhoneNumber("existingPhoneNumber");
        final UUID id = UUID.randomUUID();

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(id);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(new User()));
        when(userRepository.existsByPhoneNumber("existingPhoneNumber")).thenReturn(true);

        DuplicateDataException exception =  assertThrows(DuplicateDataException.class, () -> userService.updateUser(anyString(), patchDto));
        verify(userRepository, never()).save(any());
        assertThat(exception.getMessage(),containsString("The phone number you entered already exists"));
    }

    @Test
    void shouldEncodePasscodeWhenDtoFieldIsNotNull() {
        final UserPatchDto patchDto = new UserPatchDto();
        patchDto.setPassword("password");

        when(securityService.extractUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(new User()));

        userService.updateUser(anyString(), patchDto);

        verify(passwordEncoder, times(1)).encode(anyString());
    }

}