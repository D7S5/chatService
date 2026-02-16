package com.example.chatService.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpiredListener implements MessageListener {

    private static final String TTL_KEY_PREFIX = "online:ttl:";

    // Redis 키 만료시 이벤트
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());

        if (!expiredKey.startsWith(TTL_KEY_PREFIX)) return;

        String userId = expiredKey.replace(TTL_KEY_PREFIX, "");
        log.info("❌ User offline => {}", userId);
    }
}
