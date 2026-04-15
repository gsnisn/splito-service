package com.splito.dto.mapper;

import com.splito.dto.response.ApiMessageResponse;
import org.springframework.stereotype.Component;

@Component
public class ApiResponseMapper {

    public ApiMessageResponse message(String msg) {
        return new ApiMessageResponse(msg);
    }
}
