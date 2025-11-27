package teamfive.client;

import dto.InputHitDto;
import dto.StatDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    public List<StatDto> getStats(String start,
                                  String end,
                                  List<String> uris,
                                  Boolean unique) {

        log.info("Полученные параметры           \nstart=   {}        \nend= {}        \nuris= {}        \nunique = {}", start,
                 end, uris, unique );


        if (start == null || end == null) {
            log.warn("Параметры start и end не могут быть null");
            return List.of();
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(serverUrl + "/stats")
                                .queryParam("start", encodeValue(start))
                                .queryParam("end", encodeValue(end))
                                .queryParam("unique", unique);
                        if (uris != null && !uris.isEmpty()) {
                            uriBuilder.queryParam("uris", String.join(",", uris));
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatDto>>() {
                    });
        } catch (Exception e) {
            log.error("Ошибка при получении статистики. {}", e.getMessage());
        }
        return List.of();
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