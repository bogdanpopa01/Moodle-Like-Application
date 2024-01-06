package com.ing.hubs.dto.user;

import com.ing.hubs.model.entity.user.Gender;

import com.ing.hubs.model.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String username;
    private Role role;
    private String email;
    private String phoneNumber;
}
