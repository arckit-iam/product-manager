// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import secop.ppm1.domain.service.OrderSchedule;

class OrderScheduleConfigTest {

    private final OrderScheduleConfig config = new OrderScheduleConfig();

    @Test
    void providesAUtcClock() {
        assertEquals(ZoneOffset.UTC, config.clock().getZone());
    }

    @Test
    void countsDaysInTheConfiguredZone() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-15T23:30:00Z"), ZoneOffset.UTC);

        OrderSchedule paris = config.orderSchedule(clock, "Europe/Paris");

        assertEquals(LocalDate.of(2026, 6, 16), paris.today());
    }
}
