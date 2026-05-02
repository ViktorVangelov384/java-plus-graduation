package ru.practicum.analyzer.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.practicum.analyzer.serializer.EventSimilarityAvroDeserializer;
import ru.practicum.analyzer.serializer.UserActionAvroDeserializer;

import java.time.Duration;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;
    @Value("${kafka.consumer.poll.timeout}")
    private long pollTimeout;
    private final KafkaTopicsProperties topicsProperties;

    @Bean
    @Scope("prototype")
    public KafkaClient kafkaClient() {
        return new KafkaClient() {
            private KafkaConsumer<Long, SpecificRecordBase> actionConsumer;
            private KafkaConsumer<Long, SpecificRecordBase> similarityConsumer;

            @Override
            public KafkaConsumer<Long, SpecificRecordBase> getActionConsumer() {
                if (actionConsumer == null) {
                    Properties props = new Properties();
                    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class.getName());
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "stats.analyzer.action");
                    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
                    actionConsumer = new KafkaConsumer<>(props);
                }
                return actionConsumer;
            }

            @Override
            public KafkaConsumer<Long, SpecificRecordBase> getSimilarityConsumer() {
                if (similarityConsumer == null) {
                    Properties props = new Properties();
                    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityAvroDeserializer.class.getName());
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "stats.analyzer.similarity");
                    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
                    similarityConsumer = new KafkaConsumer<>(props);
                }
                return similarityConsumer;
            }

            @Override
            public Duration getPollTimeout() {
                return Duration.ofMillis(pollTimeout);
            }

            @Override
            public KafkaTopicsProperties getTopicsProperties() {
                return topicsProperties;
            }
        };
    }
}

