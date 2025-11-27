package teamfive.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundException(final NotFoundException e) {
        String reason = "The required object was not found.";
        log.error("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND, reason, e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse duplicatedException(final DuplicatedException e) {
        String reason = "The object already exists";
        log.error("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT, reason, e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationException(final ValidationException e) {
        String reason = "Данные не прошли проверку";
        log.error("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST, reason, e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler(java.lang.IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleStandardIllegalArgumentException(java.lang.IllegalArgumentException e) {
        String reason = "Incorrectly made request.";
        log.warn("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST, reason, e.getMessage(), getStackTrace(e));
    }

    //ToDO: Андрей - обсудить с Кириллом, что это? и зачем?
    // Добавил что бы обновление проверить можно и убрать
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleUserNotFound(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Ошибка валидации.");
        problem.setProperty("error", "Ошибка валидации.");
        return problem;
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflictException(final ConflictException e) {
        String reason = "For the requested operation the conditions are not met.";
        log.error("{}. {}", reason, e.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT, reason, e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException e) {
        String reason = "Internal Server Error";
        log.error("{}. {}", reason, e.getMessage(), e);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, reason,
                "Внутренняя ошибка сервера", getStackTrace(e));
    }
}