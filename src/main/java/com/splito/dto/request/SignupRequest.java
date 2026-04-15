package com.splito.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name too long")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 255, message = "email too long")
    private String email;

    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$",
            message = "phone must be 10-15 digits (optionally starting with +)")
    private String phone;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 72, message = "password must be 6-72 chars")
    private String password;
}
