package com.spring.eventhub.users.service;

import java.util.Optional;

import com.spring.eventhub.users.dto.UserResponseDto;

public interface UserService {

     Optional<UserResponseDto> getUser(String apiKey);

}
