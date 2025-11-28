package teamfive.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.InputHitDto;
import dto.StatDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class StatClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;


    private StatClient statClient;
    private ObjectMapper objectMapper;
    private final String serverUrl = "http://localhost:9090";
    private final String appName = "ewm-service";

    @BeforeEach
    void setUp() {
        statClient = new StatClient(restClient, serverUrl, appName);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private InputHitDto createTestHitDto() {
        InputHitDto hitDto = new InputHitDto();
        hitDto.setApp("ewm-main-service");
        hitDto.setUri("/events/1");
        hitDto.setIp("192.168.1.1");
        hitDto.setTimestamp(LocalDateTime.now());
        return hitDto;
    }

    private StatDto createStatDto(String app, String uri, Long hits) {
        StatDto statDto = new StatDto();
        statDto.setApp(app);
        statDto.setUri(uri);
        statDto.setHits(hits);
        return statDto;
    }
}
