package com.pets.hall.web;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class VisitLimits {
    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private final Clock clock;

    public VisitLimits() {
        this(System::currentTimeMillis);
    }

    VisitLimits(Clock clock) {
        this.clock = clock;
    }

    public boolean allow(String key, int max, Duration window) {
        long now = clock.now();
        Deque<Long> deque = hits.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (deque) {
            prune(deque, now - window.toMillis());
            if (deque.size() >= max) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    public void undo(String key) {
        Deque<Long> deque = hits.get(key);
        if (deque == null) {
            return;
        }
        synchronized (deque) {
            if (!deque.isEmpty()) {
                deque.removeLast();
            }
        }
    }

    private static void prune(Deque<Long> deque, long cutoff) {
        while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
            deque.removeFirst();
        }
    }

    interface Clock {
        long now();
    }
}
