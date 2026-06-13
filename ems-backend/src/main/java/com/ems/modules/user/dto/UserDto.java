package com.ems.modules.user.dto;

import com.ems.common.enums.Role;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;
    private UUID factoryId;
}
