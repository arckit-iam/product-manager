// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

/** Origine d'un compte connu de product-manager. */
public enum AccountSource {
    /** Créé par l'API (et provisionné via arc-publisher). */
    API,
    /** Découvert dans les événements consolidés (arc.sync.identity). */
    DISCOVERED
}
