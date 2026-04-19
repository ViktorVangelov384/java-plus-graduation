package ru.practicum.collector.handler;


import ru.practicum.ewm.stats.proto.event.UserActionProto;

public interface UserActionHandler {

    void handle(UserActionProto userActionProto);
}
