package com.ing.hubs.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDto {
    @NotBlank(message = "Username cannot be blank")
    @Size(max = 40, message = "Username must be at most 40 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(max = 40, message = "Password must be at most 40 characters")
    private String password;

}
