package com.example.chatService.config;

import com.example.chatService.dto.GroupMessageDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GroupKafkaConsumerConfig {

    @Bean(name = "groupConsumerFactory")
    public ConsumerFactory<String, GroupMessageDto> groupConsumerFactory() {
        Map<String, Object> props =
                new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-chat-service");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        JsonDeserializer<GroupMessageDto> valueDeserializer =
                new JsonDeserializer<>(GroupMessageDto.class);
        valueDeserializer.addTrustedPackages("com.example.chatService.dto");
        valueDeserializer.setUseTypeHeaders(false); // 타입 헤더 없이도 DTO 고정으로 역직렬화

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean(name = "groupKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, GroupMessageDto>
    groupKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, GroupMessageDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(groupConsumerFactory());
        return factory;
    }
}
