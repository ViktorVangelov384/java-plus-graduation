package ru.practicum.event.dto;

public class RecommendationResponseDto {
    private long eventId;
    private long score;

    public RecommendationResponseDto() {}

    public RecommendationResponseDto(long eventId, long score) {
        this.eventId = eventId;
        this.score = score;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}

