package ru.practicum.aggregator.serializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.stats.avro.UserActionAvro;

public class UserActionAvroDeserializer implements Deserializer<SpecificRecordBase> {

    @Override
    public SpecificRecordBase deserialize(String topic, byte[] data) {
        try {
            SpecificDatumReader<SpecificRecordBase> reader = new SpecificDatumReader<>(
                    UserActionAvro.getClassSchema()
            );
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка десериализации Avro", e);
        }
    }
}