package com.splito.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name too long")
    private String name;

    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$",
            message = "phone must be 10-15 digits (optionally starting with +)")
    private String phone;
}
