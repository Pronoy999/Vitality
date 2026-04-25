package com.vitality.common.utils;

import com.vitality.common.dtos.ErrorResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseGenerator {
    /**
     * Method to generate Failure Response.
     *
     * @param status:  the error message.
     * @param message: the {@link HttpStatus} code.
     * @return the ResponseEntity object with appropriate Error code.
     */
    public static ResponseEntity<?> generateFailureResponse(@NotNull final HttpStatus status, final String message) {
        ErrorResponse response = new ErrorResponse(status.toString(), message);
        return switch (status) {
            case BAD_REQUEST -> ResponseEntity.badRequest().body(response);
            case INTERNAL_SERVER_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            default -> ResponseEntity.status(status).body(response);
        };
    }

    /**
     * Method to generate a successful response.
     *
     * @param response: the response body.
     * @param status:   the HTTP status code.
     * @return the ResponseEntity.
     */
    public static ResponseEntity<?> generateSuccessResponse(@NotNull final Object response, HttpStatus status) {
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Method to generate a successful media response (e.g., PDF).
     *
     * @param response:              the byte array of the media content.
     * @param status:                the HTTP status code.
     * @param fileNameWithExtension: the name of the file to be downloaded (e.g., "invoice.pdf").
     * @return the ResponseEntity with appropriate headers for file download.
     */
    public static ResponseEntity<?> generateSuccessMediaResponse(final byte[] response, HttpStatus status, String fileNameWithExtension) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileNameWithExtension)
                .contentType(MediaType.APPLICATION_PDF)
                .body(response);
    }
}
