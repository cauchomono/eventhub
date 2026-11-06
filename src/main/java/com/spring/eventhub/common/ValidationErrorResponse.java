package com.spring.eventhub.common;

import java.util.List;

public record ValidationErrorResponse(List<String> message, String error, int statusCode) {

}
