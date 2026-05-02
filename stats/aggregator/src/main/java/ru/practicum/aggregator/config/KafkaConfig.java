package ru.practicum.aggregator.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.practicum.aggregator.serializer.GeneralAvroSerializer;
import ru.practicum.aggregator.serializer.UserActionAvroDeserializer;

import java.time.Duration;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;
    @Value("${kafka.consumer.group}")
    private String group;
    @Value("${kafka.consumer.poll.timeout}")
    private long pollTimeout;
    private final KafkaTopicsProperties topicsProperties;

    @Bean
    @Scope("prototype")
    public KafkaClient kafkaClient() {
        return new KafkaClient() {
            private KafkaConsumer<Long, SpecificRecordBase> consumer;
            private KafkaProducer<Long, SpecificRecordBase> producer;

            @Override
            public KafkaConsumer<Long, SpecificRecordBase> getConsumer() {
                if (consumer == null) {
                    Properties props = new Properties();
                    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class.getName());
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
                    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
                    consumer = new KafkaConsumer<>(props);
                }
                return consumer;
            }

            @Override
            public KafkaProducer<Long, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    Properties props = new Properties();
                    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
                    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
                    producer = new KafkaProducer<>(props);
                }
                return producer;
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

