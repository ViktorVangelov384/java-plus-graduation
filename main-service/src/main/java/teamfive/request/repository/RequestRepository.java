package teamfive.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamfive.request.model.ParticipationRequest;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
}
