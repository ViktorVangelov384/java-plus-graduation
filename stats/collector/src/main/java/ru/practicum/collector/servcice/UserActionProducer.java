package ru.practicum.collector.servcice;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.collector.config.KafkaTopicsProperties;
import ru.practicum.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionProducer {

    private final Producer<Long, SpecificRecordBase> producer;
    private final KafkaTopicsProperties kafkaTopics;

    private void send(String topic, SpecificRecordBase event, long timestamp, Long key) {
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                topic, null, timestamp, key, event);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки: topic={}, key={}", topic, key, exception);
            } else {
                log.debug("Отправлено: topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    public void sendUserAction(SpecificRecordBase userAction) {
        UserActionAvro avroAction = (UserActionAvro) userAction;
        Long userId = avroAction.getUserId();
        long timestamp = avroAction.getTimestamp().toEpochMilli();
        send(kafkaTopics.getStatsUserActionV1(), avroAction, timestamp, userId);
    }
}

