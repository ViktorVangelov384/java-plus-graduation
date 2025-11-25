package teamfive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import teamfive.client.StatClient;

@SpringBootApplication
@ComponentScan(basePackages = {
        "teamfive",
        "teamfive.client",
        "dto"
})

@Slf4j
public class MainApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MainApplication.class, args);
        StatClient statClient = context.getBean(StatClient.class);
        try {
            log.info("main-service: тестирование StatClient после запуска");

            String request = statClient.sayHello("main - service");
            log.info("stat-client ответил {}", request);
            log.info("main-service: stat-client ok");

        } catch (Exception e) {
            log.info("main-service: ошибка при использовании StatClient: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
