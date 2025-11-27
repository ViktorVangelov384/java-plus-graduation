package teamfive.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.InputHitDto;
import dto.StatDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    /*@Test
    void getStats_ShouldBuildCorrectUri() {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-02 00:00:00";
        List<String> uris = List.of("/events/1", "/events/2");
        Boolean unique = true;

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body((Class<Object>) any())).thenReturn(List.of());

        statClient.getStats(start, end, uris, unique);

        verify(requestHeadersUriSpec).uri(any(Function.class));
    }*/

    /*@Test
    void getStats_WithEmptyUris_ShouldHandleEmptyList() {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-02 00:00:00";
        List<String> uris = List.of();
        Boolean unique = true;

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body((Class<Object>) any())).thenReturn(List.of());

        List<StatDto> result = statClient.getStats(start, end, uris, unique);

        assertThat(result).isEmpty();
        verify(restClient).get();
    }*/

    /*@Test
    void getStats_WithMultipleUris_ShouldHandleMultipleUris() {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-02 00:00:00";
        List<String> uris = List.of("/events/1", "/events/2", "/events/3");
        Boolean unique = false;

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body((Class<Object>) any())).thenReturn(List.of());

        List<StatDto> result = statClient.getStats(start, end, uris, unique);

        assertThat(result).isEmpty();
        verify(restClient).get();
    }*/

    /*@Test
    void getStats_WithNullUris_ShouldHandleNull() {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-02 00:00:00";
        List<String> uris = null;
        Boolean unique = true;

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body((Class<Object>) any())).thenReturn(List.of());

        List<StatDto> result = statClient.getStats(start, end, uris, unique);

        assertThat(result).isEmpty();
        verify(restClient).get();
    }*/

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
