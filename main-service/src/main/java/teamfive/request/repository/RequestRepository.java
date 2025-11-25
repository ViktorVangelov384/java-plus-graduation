package teamfive.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamfive.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long userId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, String status);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);
}
