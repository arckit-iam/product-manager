// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Data;

/** Compte non utilisateur (compte technique) d'un produit. */
@Data
public class Account {
    Long id;
    String identifier;
    String type;
    /** Produit auquel le compte est corrélé ; {@code null} tant qu'il n'est pas rattaché. */
    String product;
    AccountSource source;
    Map<String, Object> metadata = new HashMap<>();
    /** Premier jour de validité ; {@code null} : le compte est créé tout de suite. */
    LocalDate startDate;
    /** Dernier jour de validité inclus ; {@code null} : sans fin. */
    LocalDate endDate;
    /** Ordre de création. */
    UUID orderId;
    /** Ordre de suppression planifiée à la fin de validité. */
    UUID endOrderId;
    OffsetDateTime createdAt;
}
