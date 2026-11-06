package com.spring.eventhub.users.model;

import java.util.List;

import com.spring.eventhub.users.enums.Role;

public record UserModel(
   int id,
  String name,
  String apiKeyHashed,
  List<Role>roles 
) {

}


