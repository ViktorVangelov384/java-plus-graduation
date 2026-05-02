package ru.practicum.collector.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.collector.servcice.UserActionProducer;
import ru.practicum.ewm.stats.proto.event.UserActionProto;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Instant;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserActionHandlerImpl implements UserActionHandler {

    private final UserActionProducer producer;

    @Override
    public void handle(UserActionProto userActionProto) {
        producer.sendUserAction(mapToAvro(userActionProto));
    }

    private UserActionAvro mapToAvro(UserActionProto userActionProto) {
        Instant timestamp = Instant.ofEpochSecond(
                userActionProto.getTimestamp().getSeconds(),
                userActionProto.getTimestamp().getNanos()
        );

        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(ActionTypeAvro.valueOf(userActionProto.getActionType().name()))
                .setTimestamp(timestamp)
                .build();
    }
}
