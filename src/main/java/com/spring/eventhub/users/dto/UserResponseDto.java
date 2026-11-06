package com.spring.eventhub.users.dto;

import java.util.List;

import com.spring.eventhub.users.enums.Role;

public record UserResponseDto(int id, String name,List<Role> roles) {

}
