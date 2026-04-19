package ru.practicum.stats.client;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.event.ActionTypeProto;
import ru.practicum.ewm.stats.proto.event.UserActionProto;

import java.time.Instant;

@Service
@Slf4j
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendUserAction(long userId, long eventId, ActionType actionType) {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(mapToProto(actionType))
                .setTimestamp(timestamp)
                .build();

        try {
            Empty response = client.collectUserAction(action);
            log.debug("Действие отправлено: userId={}, eventId={}, type={}", userId, eventId, actionType);
        } catch (Exception e) {
            log.error("Ошибка при отправке действия в Collector: userId={}, eventId={}, type={}, error={}",
                    userId, eventId, actionType, e.getMessage());
        }
    }

    private ActionTypeProto mapToProto(ActionType type) {
        return switch (type) {
            case ACTION_VIEW -> ActionTypeProto.ACTION_VIEW;
            case ACTION_REGISTER -> ActionTypeProto.ACTION_REGISTER;
            case ACTION_LIKE -> ActionTypeProto.ACTION_LIKE;
        };
    }
}


