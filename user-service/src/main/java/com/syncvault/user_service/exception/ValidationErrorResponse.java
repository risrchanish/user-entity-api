package com.syncvault.user_service.exception;

import java.util.Map;

public record ValidationErrorResponse(Map<String, String> error) {

}
