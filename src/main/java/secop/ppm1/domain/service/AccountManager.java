// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.service;

import java.util.List;
import java.util.Map;

import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.AccountType;

/** Gestion des comptes non utilisateurs des produits. */
public interface AccountManager {

    /** Types de compte non utilisateur configurés. */
    List<AccountType> getAccountTypes();

    List<Account> getAccounts(String productName, String typeName, boolean uncorrelatedOnly);

    Account getAccount(String identifier);

    /**
     * Crée et provisionne un compte. Le type et le produit sont déduits de l'identifiant par les regex
     * quand ils ne sont pas fournis.
     */
    Account createAccount(String identifier, String typeName, String productName, Map<String, Object> metadata);

    Account updateMetadata(String identifier, Map<String, Object> metadata);

    /** Rattache manuellement un compte à un produit. */
    Account correlate(String identifier, String productName);

    /** Rejoue la corrélation par regex sur les comptes sans produit ; retourne le nombre de comptes rattachés. */
    int correlateAll();

    void deleteAccount(String identifier);

    /**
     * Traite un événement consolidé d'identité : enregistre le compte s'il correspond à un type non
     * utilisateur, le supprime si l'événement est une suppression. Les autres comptes sont ignorés.
     */
    void discover(String identifier, String eventType);
}
