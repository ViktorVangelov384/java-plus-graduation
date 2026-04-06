package stats.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import stats.request.dto.ParticipationRequestDto;
import stats.request.model.ParticipationRequest;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "created", target = "created")
    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    @Mapping(source = "status", target = "status")
    ParticipationRequestDto toDto(ParticipationRequest request);

    default ParticipationRequestDto toDtoSafe(ParticipationRequest request) {
        if (request == null) {
            return null;
        }
        return toDto(request);
    }
}
