package com.learning.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Shared JSON serialization and one consistent API error envelope. */
public final class ApiResponses {
    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ApiResponses() {
    }

    public static <T> T read(jakarta.servlet.http.HttpServletRequest request, Class<T> type)
            throws IOException {
        try {
            return JSON.readValue(request.getInputStream(), type);
        } catch (JsonProcessingException exception) {
            throw new InvalidJsonException();
        }
    }

    public static void write(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSON.writeValue(response.getWriter(), body);
    }

    public static void error(HttpServletResponse response, int status, String code, String message) throws IOException {
        write(response, status, new ErrorEnvelope(new ApiError(code, message)));
    }

    public record ErrorEnvelope(ApiError error) {
    }

    public record ApiError(String code, String message) {
    }

    public static final class InvalidJsonException extends IOException {
        public InvalidJsonException() {
            super("Request body is not valid JSON.");
        }
    }
}
