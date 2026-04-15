package com.splito.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CreateGroupRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name too long")
    private String name;

    @NotEmpty(message = "memberIds are required")
    private List<@NotNull(message = "memberIds cannot contain null") Long> memberIds;
}
