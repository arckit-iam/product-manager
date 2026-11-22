// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import secop.ppm1.domain.service.OrderSchedule;

@Configuration
public class OrderScheduleConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Les jours des dates de début et de fin se comptent dans le fuseau {@code arc.orders.zone}. */
    @Bean
    OrderSchedule orderSchedule(Clock clock, @Value("${arc.orders.zone:UTC}") String zone) {
        return new OrderSchedule(clock, ZoneId.of(zone));
    }
}
