package teamfive.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import teamfive.event.dto.EventLocationDto;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.dto.EventShortDto;
import teamfive.event.model.Event;
import teamfive.event.model.EventLocation;
import teamfive.event.model.EventState;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "state", source = "state", qualifiedByName = "stateToString")
    EventResponseDto toEventResponseDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    EventShortDto toEventShortDto(Event event);

    EventLocationDto toEventLocationDto(EventLocation location);

    EventLocation toEventLocation(EventLocationDto locationDto);

    @Named("stateToString")
    default String stateToString(EventState state) {
        return state != null ? state.name() : null;
    }
}
