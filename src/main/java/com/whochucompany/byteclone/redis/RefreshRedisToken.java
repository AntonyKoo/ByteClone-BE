package com.whochucompany.byteclone.redis;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash("RefreshToken")
public class RefreshRedisToken {
    @Id
    private Long userId;
    private String token;

    @Builder
    private RefreshRedisToken(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    static public RefreshRedisToken createToken(Long userId, String token){
        return new RefreshRedisToken(userId, token);
    }

    public void reissue(String token) {
        this.token = token;
    }
}
