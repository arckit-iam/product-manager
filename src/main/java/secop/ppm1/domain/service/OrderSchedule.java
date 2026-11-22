// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Temporalité des comptes et des profils : une date de début déclenche la création au début de ce jour,
 * une date de fin (dernier jour inclus) déclenche la suppression au début du jour suivant. Les jours se
 * comptent dans le fuseau configuré.
 */
public class OrderSchedule {

    private final Clock clock;
    private final ZoneId zone;

    public OrderSchedule(Clock clock, ZoneId zone) {
        this.clock = clock;
        this.zone = zone;
    }

    public LocalDate today() {
        return LocalDate.now(clock.withZone(zone));
    }

    public OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    /** Instant d'exécution de la création : immédiat sans date de début ou si elle est atteinte. */
    public OffsetDateTime startAt(LocalDate startDate) {
        if (startDate == null) {
            return now();
        }
        OffsetDateTime at = startDate.atStartOfDay(zone).toOffsetDateTime();
        return at.isAfter(now()) ? at : now();
    }

    /** Instant d'exécution de la suppression : début du jour suivant la date de fin. */
    public OffsetDateTime endAt(LocalDate endDate) {
        OffsetDateTime at = endDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        return at.isAfter(now()) ? at : now();
    }

    /** Vrai si la création n'est plus en attente : pas de date de début, ou date atteinte. */
    public boolean hasStarted(LocalDate startDate) {
        return startDate == null || !startDate.isAfter(today());
    }

    /** Vrai si le dernier jour de validité est passé : la suppression planifiée est due. */
    public boolean hasEnded(LocalDate endDate) {
        return endDate != null && endDate.isBefore(today());
    }
}
