// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class OrderScheduleTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T23:30:00Z"), ZoneOffset.UTC);
    private final OrderSchedule utc = new OrderSchedule(clock, ZoneOffset.UTC);

    @Test
    void creationIsImmediateWithoutStartDateOrWhenReached() {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-15T23:30:00Z");

        assertEquals(now, utc.startAt(null));
        assertEquals(now, utc.startAt(LocalDate.of(2026, 6, 15)));
        assertEquals(now, utc.startAt(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void creationWaitsForTheStartOfAFutureStartDate() {
        assertEquals(OffsetDateTime.parse("2026-06-20T00:00:00Z"), utc.startAt(LocalDate.of(2026, 6, 20)));
    }

    @Test
    void deletionHappensAtTheStartOfTheDayAfterTheEndDate() {
        assertEquals(OffsetDateTime.parse("2026-06-21T00:00:00Z"), utc.endAt(LocalDate.of(2026, 6, 20)));
        assertEquals(OffsetDateTime.parse("2026-06-15T23:30:00Z"), utc.endAt(LocalDate.of(2026, 6, 10)));
    }

    @Test
    void daysAreCountedInTheConfiguredZone() {
        OrderSchedule paris = new OrderSchedule(clock, ZoneId.of("Europe/Paris"));

        assertEquals(LocalDate.of(2026, 6, 15), utc.today());
        assertEquals(LocalDate.of(2026, 6, 16), paris.today());
        assertEquals(OffsetDateTime.parse("2026-06-19T22:00:00Z").toInstant(),
                paris.startAt(LocalDate.of(2026, 6, 20)).toInstant());
    }

    @Test
    void startedAndEndedStatuses() {
        assertTrue(utc.hasStarted(null));
        assertTrue(utc.hasStarted(LocalDate.of(2026, 6, 15)));
        assertFalse(utc.hasStarted(LocalDate.of(2026, 6, 16)));

        assertFalse(utc.hasEnded(null));
        assertFalse(utc.hasEnded(LocalDate.of(2026, 6, 15)));
        assertTrue(utc.hasEnded(LocalDate.of(2026, 6, 14)));
    }
}
