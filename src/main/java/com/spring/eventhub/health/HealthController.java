package com.spring.eventhub.health;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.eventhub.common.decorators.NoEnvelopeBody;

@RestController
@RequestMapping("health")
public class HealthController {

    @GetMapping
    @NoEnvelopeBody 
    public ResponseEntity<HealthResponse> getHeath(
            @RequestHeader(value = "x-client-version", required = false) String clientVersion) {
        HealthResponse health = new HealthResponse(clientVersion, "ok", "Event hub is running", Instant.now());
        return ResponseEntity.ok(health);

    }

}
