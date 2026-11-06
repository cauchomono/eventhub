package com.spring.eventhub.users.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.spring.eventhub.users.model.UserModel;
import com.spring.eventhub.users.dto.UserResponseDto;
import com.spring.eventhub.users.enums.Role;

@Service
public class UserServiceImpl implements UserService {
    final ApiKeyHasher apiKeyHasher;
    final List<UserModel> userList;
    final Map<String, UserModel> userMap;

    public UserServiceImpl(ApiKeyHasher apiKeyHasher) {
        this.apiKeyHasher = apiKeyHasher;

        userList = List.of(
                addUser(1, "John Doe", "secret-api-key", List.of(Role.ROLE_ADMIN)),
                addUser(2, "Jane Smith", "secret-api-key2", List.of(Role.ROLE_ORGANIZER)),
                addUser(3, "Bob Johnson", "secret-api-key3", List.of(Role.ROLE_ATTENDEE)),
                // Attendees extra solo para pruebas (reservas ajenas); no forman parte del contrato.
                addUser(4, "Alice Brown", "secret-api-key4", List.of(Role.ROLE_ATTENDEE)),
                addUser(5, "Carol White", "secret-api-key5", List.of(Role.ROLE_ATTENDEE)));
        userMap = getUsers();

    }

    private Map<String, UserModel> getUsers() {
        return userList.stream().collect(Collectors.toMap(UserModel::apiKeyHashed, user -> user));
    }

    public Optional<UserResponseDto> getUser(String apiKey) {
        UserModel user = userMap.get(apiKeyHasher.hashHmacSHA256(apiKey));

        return Optional.ofNullable(user).map(u -> new UserResponseDto(u.id(), u.name(), u.roles()));
    }

    private UserModel addUser(int id, String name, String apiKey, List<Role> roles) {
        return new UserModel(id, name, apiKeyHasher.hashHmacSHA256(apiKey), roles);
    }

}
