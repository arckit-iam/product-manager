// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import java.time.LocalDate;

/** Corps de changement des dates : {@code null} efface la date correspondante. */
public record DatesRequest(LocalDate startDate, LocalDate endDate) {
}
