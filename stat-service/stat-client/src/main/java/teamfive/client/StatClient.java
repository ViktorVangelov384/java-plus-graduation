package teamfive.client;

import dto.InputHitDto;
import dto.StatDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatClient {
    private final String serverUrl;
    private final String appName;
    private final RestClient restClient;

    public StatClient(RestClient restClient,
                      @Value("${stats-server-url}") String serverUrl,
                      @Value("${stat.app-name:ewm-service}") String appName) {
        this.restClient = restClient;
        this.serverUrl = serverUrl;
        this.appName = appName;
    }

    public String sayHello(String iName) {
        if (iName.isEmpty()) {
            return "Привет тебе, чудо - незнакомец! тебя приветствует самый лучший в мире stat-server!";
        }
        return "Привет тебе, " + iName + "! тебя приветствует самый лучший в мире stat-server!";
    }

    public void hit(HttpServletRequest request) {
        try {
            InputHitDto hitDto = new InputHitDto();
            hitDto.setApp(appName);
            hitDto.setUri(request.getRequestURI());
            hitDto.setIp(getClientIpAddress(request));
            hitDto.setTimestamp(LocalDateTime.now());
            log.warn("StatClient - CTAT");

            restClient.post().uri(serverUrl + "/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hitDto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Ошибка при отправке hit. {}", e.getMessage());
        }
    }

    public List<StatDto> getStats(ParamRequest paramRequest) {

        try {
            log.info("Получение статистики с параметрами: start={}, end={}, uris={}, unique={}",
                    paramRequest.getStart(), paramRequest.getEnd(), paramRequest.getUris(), paramRequest.getUnique());

            String baseUrl = serverUrl;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String startFormatted = paramRequest.getStart().format(formatter);
            String endFormatted = paramRequest.getEnd().format(formatter);

            String finalUrl = String.format("%s/stats?start=%s&end=%s&unique=%s",
                    baseUrl, startFormatted, endFormatted, paramRequest.getUnique());

            if (paramRequest.getUris() != null && !paramRequest.getUris().isEmpty()) {
                for (String uri : paramRequest.getUris()) {
                    finalUrl += "&uris=" + uri;
                }
            }

            log.info("Final URL: {}", finalUrl);

            return restClient.get()
                    .uri(finalUrl)
                    .retrieve()
                    .onStatus(status -> status != HttpStatus.OK, (request, response) -> {
                        String errorBody = "Не удалось прочитать тело ошибки";
                        try {
                            errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            log.error("Ошибка при чтении тела ответа: {}", e.getMessage());
                        }
                        log.error("Ошибка при запросе к серверу: {}: {}", response.getStatusCode().value(), errorBody);
                        throw new RuntimeException("Ошибка статистики: " + response.getStatusCode().value() + ": " + errorBody);
                    })
                    .body(new ParameterizedTypeReference<List<StatDto>>() {
                    });
        } catch (Exception e) {
            log.error("Исключение при запросе статистики: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при запросе статистики: " + e.getMessage(), e);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {

        String xForwardedForHeader = request.getHeader("X-Forwarded-For");

        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}