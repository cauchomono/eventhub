package com.spring.eventhub.health;


import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HealthResponse( @JsonProperty("x-client-version") String xclientVersion, String status, String message, Instant timestamp) {


}
