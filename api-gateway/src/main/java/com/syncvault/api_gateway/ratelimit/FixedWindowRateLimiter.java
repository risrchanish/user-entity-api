package com.syncvault.api_gateway.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FixedWindowRateLimiter {

    private static final long WINDOW_SIZE_MILLIS = 60_000L;

    //Key -> [windowStart, count]
    private final ConcurrentHashMap<String, long[]> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public FixedWindowRateLimiter(){
        this(Clock.systemUTC());
    }

    FixedWindowRateLimiter(Clock clock){
        this.clock = clock;
    }

    public boolean tryAcquire(String key, int limit){

        long now = clock.millis();
        long currentWindowStart = (now / WINDOW_SIZE_MILLIS) * WINDOW_SIZE_MILLIS;

        long[] result = windows.compute(key, (k, existing) ->{
            if(existing == null || existing[0] < currentWindowStart){
                return new long[] {currentWindowStart, 1L};
            }
            existing[1]++;
            return existing;
        });
        return result[1] <= limit;
    }


}
