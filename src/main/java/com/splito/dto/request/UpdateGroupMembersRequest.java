package com.splito.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class UpdateGroupMembersRequest {

    @NotEmpty(message = "memberIds is required")
    private List<@NotNull(message = "memberIds cannot contain null") Long> memberIds;
}
