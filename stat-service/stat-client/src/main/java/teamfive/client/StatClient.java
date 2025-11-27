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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
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
        log.warn("ТЫ В ТАНЦАХ????????");

        // Добавим логи для отслеживания параметров запроса
        log.info("Start: " + paramRequest.getStart());
        log.info("End: " + paramRequest.getEnd());
        log.info("URIs: " + paramRequest.getUris());
        log.info("Unique: " + paramRequest.getUnique());

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(serverUrl + "/stats")
                        .queryParam("start", paramRequest.getStart().toString())
                        .queryParam("end", paramRequest.getEnd().toString())
                        .queryParam("uris", paramRequest.getUris())
                        .queryParam("unique", paramRequest.getUnique())
                        .build())
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, (request, response) -> {
                    log.error("Ошибка при запросе к серверу: " + response.getStatusCode().value() + ": " + response.getBody());
                    throw new RuntimeException(response.getStatusCode().value() + ": " + response.getBody());
                })
                .body(ParameterizedTypeReference.forType(List.class));
    }


    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String getClientIpAddress(HttpServletRequest request) {

        String xForwardedForHeader = request.getHeader("X-Forwarded-For");

        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}