package com.ems.modules.auth.dto;

import com.ems.modules.user.dto.UserDto;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;
}
