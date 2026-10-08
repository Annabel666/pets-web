package com.pets.hall.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class VisitLimitsTest {
    @Test
    void blocksAfterTheWindowFillsAndOpensAgainLater() {
        long[] now = {1_000L};
        VisitLimits limits = new VisitLimits(() -> now[0]);
        assertTrue(limits.allow("login", 2, Duration.ofMillis(100)));
        assertTrue(limits.allow("login", 2, Duration.ofMillis(100)));
        assertFalse(limits.allow("login", 2, Duration.ofMillis(100)));
        now[0] = 1_200L;
        assertTrue(limits.allow("login", 2, Duration.ofMillis(100)));
    }

    @Test
    void undoReleasesTheSlot() {
        VisitLimits limits = new VisitLimits(() -> 5_000L);
        assertTrue(limits.allow("register", 1, Duration.ofHours(1)));
        assertFalse(limits.allow("register", 1, Duration.ofHours(1)));
        limits.undo("register");
        assertTrue(limits.allow("register", 1, Duration.ofHours(1)));
    }
}
