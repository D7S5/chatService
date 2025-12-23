package com.example.chatservice.config;

import com.example.chatservice.dto.GroupMessageDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GroupKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, GroupMessageDto> groupProducerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  JsonSerializer.class);

//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
//        props.put("schema.registry.url", "http://localhost:8081");

        return new DefaultKafkaProducerFactory(props);
    }

    @Bean
    public KafkaTemplate<String, GroupMessageDto> groupKafkaTemplate() {
        return new KafkaTemplate<>(groupProducerFactory());
    }
}
