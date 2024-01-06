package com.ing.hubs.dto.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ing.hubs.deserielize.LocalDateDeserializer;
import com.ing.hubs.model.entity.user.Gender;
import com.ing.hubs.deserielize.GenderDeserializer;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @NotBlank(message = "First name cannot be blank")
    @Pattern(regexp = "^[A-Za-z-]+$", message = "First name must contain only letters")
    @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Pattern(regexp = "^[A-Za-z-]+$", message = "Last name must contain only letters")
    @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
    private String lastName;

    @NotNull(message = "Gender cannot be null")
    @JsonDeserialize(using = GenderDeserializer.class)
    private Gender gender;

    @NotNull(message = "Date of birth cannot be null")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 6, max = 50, message = "Username must be between 6 and 50 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(regexp = "^[a-zA-Z]+[.][a-zA-Z0-9]+@(?:stud[.])?poodle[.]com$", message = "Invalid email format")
    @Size(min = 3, max = 50, message = "Email must be between 3 and 50 characters")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\s?\\d+(\\s?\\d+)*$", message = "Invalid phone number format")
    @Size(min = 7, max = 16, message = "Phone number should be between 10 and 15 in length")
    private String phoneNumber;
}
