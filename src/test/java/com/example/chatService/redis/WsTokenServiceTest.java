package com.example.chatService.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WsTokenServiceTest {

    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    private final WsTokenService service = new WsTokenService(redisTemplate);

    @Test
    void createTokenForUserStoresUserIdWithTwoMinuteTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = service.createTokenForUser("user-1");

        assertThat(token).isNotBlank();
        verify(valueOperations).set(
                eq("ws_token:" + token),
                eq("user-1"),
                eq(Duration.ofSeconds(120))
        );
    }

    @Test
    void consumeTokenReturnsUserIdAndDeletesTokenWhenFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("ws_token:token-1")).thenReturn("user-1");

        assertThat(service.consumeToken("token-1")).isEqualTo("user-1");

        verify(redisTemplate).delete("ws_token:token-1");
    }

    @Test
    void consumeTokenDoesNotDeleteMissingToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("ws_token:missing")).thenReturn(null);

        assertThat(service.consumeToken("missing")).isNull();

        verify(redisTemplate, never()).delete(any(String.class));
    }

    @Test
    void peekTokenReadsWithoutDeleting() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("ws_token:token-1")).thenReturn("user-1");

        assertThat(service.peekToken("token-1")).isEqualTo("user-1");

        verify(redisTemplate, never()).delete(any(String.class));
    }
}
