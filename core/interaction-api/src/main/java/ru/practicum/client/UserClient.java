package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/internal/users/{id}")
    UserShortDto getById(@PathVariable("id") Long id);

    @GetMapping("/internal/users/check/{id}")
    void checkUserExists(@PathVariable("id") Long id);

    @PostMapping("/internal/users/by-ids")
    Map<Long, UserShortDto> getAllUsersByIds(@RequestBody List<Long> userIds);
}
