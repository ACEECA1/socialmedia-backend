package org.socialmedia.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "success", "statusCode", "message", "data", "timestamp" })
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private int statusCode;
    private String message;
    private T data;
    private Object errors;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    /**
     * Creates a successful API response.
     *
     * @param data    The data to include in the response
     * @param message The success message
     * @return A successful API response
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .message(message)
                .data(data)
                .build();
    }
    /**
     * Creates a successful API response with a custom status code.
     *
     * @param successCode The HTTP status code for the successful response
     * @param data        The data to include in the response
     * @param message     The success message
     * @return A successful API response with a custom status code
     */
    public static <T> ApiResponse<T> success(int successCode , T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(successCode)
                .message(message)
                .data(data)
                .build();
    }

    /**
    * Returns an ApiResponse object with the given parameters.
    *
    * @param success whether the response is successful or not
    * @param statusCode the HTTP status code of the response
    * @param message the message to include in the response
    * @param data the data to include in the response
    * @param errors the list of errors to include in the response
    * @return an ApiResponse object with the given parameters
    * */
    public static <T> ApiResponse<T> respond(boolean success, int statusCode, String message, T data , List<String> errors) {
        return ApiResponse.<T>builder()
                .success(success)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .errors(errors)
                .build();
    }
    /**
     * Returns an ApiResponse object with the given parameters.
     *
     * @param success whether the response is successful or not
     * @param statusCode the HTTP status code of the response
     * @param message the message to include in the response
     * @param data the data to include in the response
     * @return an ApiResponse object with the given parameters
     * */
    public static <T> ApiResponse<T> respond(boolean success, int statusCode, String message, T data) {
        return ApiResponse.<T>builder()
                .success(success)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates an error API response.
     *
     * @param message The error message
     * @param errors  The list of error details
     * @return An error API response
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
    public ApiResponse(boolean success, int statusCode, String message , T data){
        this.success = success;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}