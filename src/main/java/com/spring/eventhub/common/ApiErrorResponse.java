package com.spring.eventhub.common;

public record ApiErrorResponse(String message, String error, int statusCode) {

}
