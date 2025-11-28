package teamfive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.InputHitDto;
import dto.OutHitDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import teamfive.storage.StatRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StatRepository statRepository;

    @Test
    void createHit_ShouldReturnCreatedHit() throws Exception {
        // Given
        InputHitDto hitDto = new InputHitDto();
        hitDto.setApp("ewm-main-service");
        hitDto.setUri("/events/1");
        hitDto.setIp("192.168.1.1");
        hitDto.setTimestamp(LocalDateTime.now());

        MvcResult result = mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value("ewm-main-service"))
                .andExpect(jsonPath("$.uri").value("/events/1"))
                .andExpect(jsonPath("$.ip").value("192.168.1.1"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        OutHitDto responseDto = objectMapper.readValue(response, OutHitDto.class);

        assertThat(responseDto.getId()).isNotNull();
    }

    @Test
    void getStats_ShouldReturnStats() throws Exception {
        InputHitDto hitDto = new InputHitDto();
        hitDto.setApp("ewm-main-service");
        hitDto.setUri("/events/1");
        hitDto.setIp("192.168.1.1");
        hitDto.setTimestamp(LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isCreated());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String start = LocalDateTime.now().minusDays(1).format(formatter);
        String end = LocalDateTime.now().format(formatter);

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").isNumber());
    }

    @Test
    void getStats_WithInvalidDateRange_ShouldReturnBadRequest() throws Exception {
        String start = LocalDateTime.now().toString().replace("T", " ");
        String end = LocalDateTime.now().minusDays(1).toString().replace("T", " ");

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isBadRequest());
    }
}