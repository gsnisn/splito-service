package com.splito.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateGroupNameRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name too long")
    private String name;
}
