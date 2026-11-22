// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class Profile {
    String product;
    String name;
    String description;
    String scope;
    /** Premier jour de validité ; {@code null} : le profil est créé tout de suite. */
    LocalDate startDate;
    /** Dernier jour de validité inclus ; {@code null} : sans fin. */
    LocalDate endDate;
    /** Ordre de création. */
    UUID orderId;
    /** Ordre de suppression planifiée à la fin de validité. */
    UUID endOrderId;
}
