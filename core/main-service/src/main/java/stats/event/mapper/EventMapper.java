package stats.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import stats.event.dto.EventLocationDto;
import stats.event.dto.EventResponseDto;
import stats.event.dto.EventShortDto;
import stats.event.model.Event;
import stats.event.model.EventLocation;
import stats.event.model.EventState;

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
