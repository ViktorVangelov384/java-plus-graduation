package ru.practicum.aggregator.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.config.KafkaClient;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorProcessor {

    private final KafkaClient kafkaClient;
    private final SimilarityCalculator calculator;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @PostConstruct
    public void init() {
        Thread processorThread = new Thread(this::start);
        processorThread.setName("kafka-aggregator-thread");
        processorThread.setDaemon(false);
        processorThread.start();
        log.info("AggregatorProcessor запущен в фоновом потоке");
    }

    private void start() {
        var consumer = kafkaClient.getConsumer();
        var producer = kafkaClient.getProducer();

        consumer.subscribe(List.of(kafkaClient.getTopicsProperties().getStatsUserActionV1()));
        log.info("Подписка на топик: {}", kafkaClient.getTopicsProperties().getStatsUserActionV1());

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<Long, SpecificRecordBase> records = consumer.poll(kafkaClient.getPollTimeout());

                for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                    handleRecord(record, producer);
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
            log.error("Ошибка в цикле обработки Kafka", e);
        } finally {
            consumer.close();
            producer.close();
        }
    }

    private void handleRecord(ConsumerRecord<Long, SpecificRecordBase> record, Producer<Long, SpecificRecordBase> producer) {
        if (record.value() instanceof UserActionAvro action) {
            log.info(">>> Получено действие: userId={}, eventId={}, type={}", action.getUserId(), action.getEventId(),
                    action.getActionType());
            List<EventSimilarityAvro> similarities = calculator.calculateSimilarity(action);

            for (EventSimilarityAvro similarity : similarities) {
                ProducerRecord<Long, SpecificRecordBase> outRecord = new ProducerRecord<>(
                        kafkaClient.getTopicsProperties().getStatsEventsSimilarityV1(),
                        null,
                        similarity.getEventA(),
                        similarity
                );
                producer.send(outRecord, (metadata, ex) -> {
                    if (ex != null) log.error("Ошибка отправки сходства", ex);
                    else log.info("Отправлено сходство: {} <-> {}", similarity.getEventA(), similarity.getEventB());
                });
            }
        }
    }
}

