package ru.practicum.analyzer.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;

import java.time.Duration;

public interface KafkaClient {

    Consumer<Long, SpecificRecordBase> getActionConsumer();

    Consumer<Long, SpecificRecordBase> getSimilarityConsumer();

    Duration getPollTimeout();

    KafkaTopicsProperties getTopicsProperties();
}

