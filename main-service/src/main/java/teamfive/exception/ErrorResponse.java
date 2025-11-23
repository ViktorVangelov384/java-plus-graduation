package teamfive.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private HttpStatus status;
    private String reason; // Общее описание причины ошибки
    private String message; // Сообщение об ошибке

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeStamp = LocalDateTime.now();

    public ErrorResponse(HttpStatus status, String reason, String message) {
        this.status = status;
        this.reason = reason;
        this.message = message;
    }
}