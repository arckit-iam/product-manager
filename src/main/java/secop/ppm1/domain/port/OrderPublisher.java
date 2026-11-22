// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.port;

import java.util.UUID;

import secop.ppm1.domain.model.ProvisioningOrder;

/** Transmet à arc-publisher les ordres de provisioning (comptes, profils), ou leur annulation. */
public interface OrderPublisher {

    /** Soumet l'ordre ; il sera exécuté à {@code order.executeAt()}. Idempotent sur {@code order.id()}. */
    void submit(ProvisioningOrder order);

    /**
     * Annule un ordre pas encore exécuté. Sans effet si l'ordre est inconnu, déjà annulé ou déjà
     * pris en charge par l'exécution.
     */
    void cancel(UUID orderId);
}
