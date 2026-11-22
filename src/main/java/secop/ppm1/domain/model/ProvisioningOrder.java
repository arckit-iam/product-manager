// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ordre de provisioning transmis à arc-publisher : création ou suppression d'un compte technique
 * ({@code IDENTITY}) ou d'un profil de produit ({@code ENTITLEMENT}), à exécuter à {@code executeAt}.
 * L'identifiant est généré par le manager : il sert à annuler l'ordre tant qu'il n'est pas exécuté.
 */
public record ProvisioningOrder(
        UUID id,
        Target target,
        Action action,
        Account account,
        String productName,
        Profile profile,
        OffsetDateTime executeAt,
        String correlationId) {

    public enum Target {
        IDENTITY, ENTITLEMENT
    }

    public enum Action {
        CREATE, DELETE
    }

    public static ProvisioningOrder forAccount(Action action, Account account, OffsetDateTime executeAt,
            String correlationId) {
        return new ProvisioningOrder(UUID.randomUUID(), Target.IDENTITY, action, account, null, null, executeAt,
                correlationId);
    }

    public static ProvisioningOrder forProfile(Action action, String productName, Profile profile,
            OffsetDateTime executeAt, String correlationId) {
        return new ProvisioningOrder(UUID.randomUUID(), Target.ENTITLEMENT, action, null, productName, profile,
                executeAt, correlationId);
    }
}
