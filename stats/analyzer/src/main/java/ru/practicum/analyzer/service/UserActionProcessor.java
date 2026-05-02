package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.config.KafkaClient;
import ru.practicum.analyzer.handler.UserActionHandler;
import ru.practicum.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionProcessor implements Runnable {

    private final KafkaClient kafkaClient;
    private final UserActionHandler handler;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        var consumer = kafkaClient.getActionConsumer();
        consumer.subscribe(List.of(kafkaClient.getTopicsProperties().getStatsUserActionV1()));

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<Long, SpecificRecordBase> records = consumer.poll(kafkaClient.getPollTimeout());
                for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                    if (record.value() instanceof UserActionAvro action) {
                        handler.handle(action);
                    }
                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );
                }
                if (!records.isEmpty()) {
                    consumer.commitSync(currentOffsets);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка в UserActionProcessor", e);
        } finally {
            consumer.close();
        }
    }
}
