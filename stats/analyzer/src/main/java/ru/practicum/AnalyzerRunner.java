package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.UserActionProcessor;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

    private final UserActionProcessor userActionProcessor;

    @Override
    public void run(String... args) {
        Thread thread = new Thread(userActionProcessor);
        thread.setName("kafka-user-action-thread");
        thread.setDaemon(false);
        thread.start();
    }
}
