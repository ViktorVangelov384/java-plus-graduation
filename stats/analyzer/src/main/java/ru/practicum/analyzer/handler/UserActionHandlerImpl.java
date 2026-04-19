package ru.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.dao.model.UserAction;
import ru.practicum.analyzer.dao.repository.UserActionRepository;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionHandlerImpl implements UserActionHandler {

    private static final Map<ActionTypeAvro, Double> WEIGHTS = Map.of(
            ActionTypeAvro.ACTION_VIEW, 0.4,
            ActionTypeAvro.ACTION_REGISTER, 0.8,
            ActionTypeAvro.ACTION_LIKE, 1.0
    );

    private final UserActionRepository repository;

    @Override
    public void handle(UserActionAvro action) {
        double weight = WEIGHTS.getOrDefault(action.getActionType(), 0.0);

        Optional<UserAction> existing = repository.findByUserIdAndEventId(action.getUserId(), action.getEventId());
        if (existing.isPresent() && existing.get().getWeight() >= weight) {
            return;
        }

        UserAction userAction = existing.orElseGet(UserAction::new);
        userAction.setUserId(action.getUserId());
        userAction.setEventId(action.getEventId());
        userAction.setWeight(weight);
        userAction.setActionTimestamp(action.getTimestamp());

        repository.save(userAction);
    }
}
