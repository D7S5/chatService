package com.example.chatService.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHashUtilTest {

    @Test
    void hashReturnsBase64EncodedSha256() throws Exception {
        String token = "refresh-token";

        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));

        assertThat(TokenHashUtil.hash(token))
                .isEqualTo(Base64.getEncoder().encodeToString(digest));
    }

    @Test
    void hashIsDeterministicAndDoesNotExposeRawToken() {
        String token = "secret-token";

        String first = TokenHashUtil.hash(token);
        String second = TokenHashUtil.hash(token);

        assertThat(first).isEqualTo(second);
        assertThat(first).doesNotContain(token);
    }
}
