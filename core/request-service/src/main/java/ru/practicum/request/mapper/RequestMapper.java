package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "created", target = "created")
    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    @Mapping(source = "status", target = "status")
    ParticipationRequestDto toDto(Request request);

    List<ParticipationRequestDto> toDtoList(List<Request> requests);
}
