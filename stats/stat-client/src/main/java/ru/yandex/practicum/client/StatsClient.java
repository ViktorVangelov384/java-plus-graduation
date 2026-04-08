package ru.yandex.practicum.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.contract.StatsOperation;
import ru.yandex.practicum.dto.StatDto;

import java.time.LocalDateTime;

@FeignClient(name = "stats-server")
public interface StatsClient extends StatsOperation {

    default void hit(HttpServletRequest request) {
        hit(request.getRemoteAddr(), request.getRequestURI());
    }

    default void hit(String ip, String uri) {
        StatDto statDto = new StatDto("main-service", uri, ip, LocalDateTime.now());
        saveHitRequest(statDto);
    }
}
