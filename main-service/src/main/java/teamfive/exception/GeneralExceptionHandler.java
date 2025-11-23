package teamfive.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundException(final NotFoundException e) {
        String reason = "Запрашиваемый объект не найден";
        log.error("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND, reason, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse duplicatedException(final DuplicatedException e) {
        String reason = "В базе данный уже есть такой объект";
        log.error("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT, reason, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationException(final ValidationException e) {
        String reason = "Данные не прошли проверку";
        log.error("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST, reason, e.getMessage());
    }

    // Добавил что бы обновление проверить можно и убрать
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleUserNotFound(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Ошибка валидации.");
        problem.setProperty("error", "Ошибка валидации.");
        return problem;
    }
}