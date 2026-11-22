// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import java.time.LocalDate;
import java.util.Map;

/** Corps de création d'un compte ; {@code type} et {@code product} sont déduits de l'identifiant s'ils sont absents. */
public record CreateAccountRequest(String identifier, String type, String product, Map<String, Object> metadata,
        LocalDate startDate, LocalDate endDate) {
}
