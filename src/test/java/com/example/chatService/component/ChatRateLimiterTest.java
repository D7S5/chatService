package com.example.chatService.component;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatRateLimiterTest {

    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final ChatRateLimiter limiter = new ChatRateLimiter(redis);

    @Test
    void allowOrBanRejectsAlreadyBannedUserWithoutIncrementingRate() {
        when(redis.hasKey("chat:ban:useruser-1")).thenReturn(true);

        assertThat(limiter.allowOrBan("user-1")).isFalse();

        verify(redis, never()).opsForValue();
    }

    @Test
    void allowOrBanSetsOneSecondWindowOnFirstMessage() {
        when(redis.hasKey("chat:ban:useruser-1")).thenReturn(false);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("chat:rate:user:user-1")).thenReturn(1L);

        assertThat(limiter.allowOrBan("user-1")).isTrue();

        verify(redis).expire("chat:rate:user:user-1", Duration.ofSeconds(1));
        verify(valueOperations, never()).set(eq("chat:ban:useruser-1"), any(), any(Duration.class));
    }

    @Test
    void allowOrBanBansUserAfterLimitIsExceeded() {
        when(redis.hasKey("chat:ban:useruser-1")).thenReturn(false);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("chat:rate:user:user-1")).thenReturn(8L);

        assertThat(limiter.allowOrBan("user-1")).isFalse();

        verify(valueOperations).set("chat:ban:useruser-1", "1", Duration.ofSeconds(30));
    }

    @Test
    void allowRoomAllowsUpToRoomLimit() {
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("chat:rate:room:room-1")).thenReturn(1000L);

        assertThat(limiter.allowRoom("room-1")).isTrue();
    }

    @Test
    void allowRoomRejectsAfterRoomLimit() {
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("chat:rate:room:room-1")).thenReturn(1001L);

        assertThat(limiter.allowRoom("room-1")).isFalse();
    }
}
