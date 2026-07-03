package com.watchnext.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.Bucket4jCaffeine;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public AsyncProxyManager<String> rateLimiterProxyManager() {
        @SuppressWarnings("unchecked")
        Caffeine<String, RemoteBucketState> caffeineBuilder = (Caffeine<
            String,
            RemoteBucketState
        >) (Caffeine<?, ?>) Caffeine.newBuilder().maximumSize(10_000);

        return Bucket4jCaffeine.<String>builderFor(caffeineBuilder)
            .expirationAfterWrite(
                ExpirationAfterWriteStrategy.fixedTimeToLive(
                    Duration.ofMinutes(1)
                )
            )
            .build()
            .asAsync();
    }
}
