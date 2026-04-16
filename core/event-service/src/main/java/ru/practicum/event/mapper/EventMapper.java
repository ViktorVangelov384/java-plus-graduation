package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.enums.EventState;
import ru.practicum.event.dto.EventLocationDto;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventLocation;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "category", source = "category")
    @Mapping(target = "state", source = "state", qualifiedByName = "stateToString")
    EventResponseDto toEventResponseDto(Event event);

    @Mapping(target = "category", source = "category")
    EventShortDto toEventShortDto(Event event);

    EventLocationDto toEventLocationDto(EventLocation location);

    @Named("stateToString")
    default String stateToString(EventState state) {
        return state != null ? state.name() : null;
    }
}
