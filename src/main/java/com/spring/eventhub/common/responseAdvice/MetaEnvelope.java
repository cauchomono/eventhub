package com.spring.eventhub.common.responseAdvice;

import java.time.Instant;
import java.util.UUID;

public record MetaEnvelope(String requestId, Instant timestamp) {

}
