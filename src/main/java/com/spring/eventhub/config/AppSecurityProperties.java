package com.spring.eventhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix="app.config.security")
@Validated
public record AppSecurityProperties(@NotBlank String pepper) {

}
