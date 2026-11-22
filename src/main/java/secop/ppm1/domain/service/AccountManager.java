// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.service;

import java.time.LocalDate;
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
     * quand ils ne sont pas fournis. La création est différée à {@code startDate} ; la suppression est
     * planifiée après {@code endDate} (dernier jour inclus). Dates facultatives.
     */
    Account createAccount(String identifier, String typeName, String productName,
            Map<String, Object> metadata, LocalDate startDate, LocalDate endDate);

    /** Création immédiate, sans fin planifiée. */
    default Account createAccount(String identifier, String typeName, String productName,
            Map<String, Object> metadata) {
        return createAccount(identifier, typeName, productName, metadata, null, null);
    }

    /** Change les dates de début et de fin d'un compte, en remplaçant ses ordres en attente. */
    Account updateDates(String identifier, LocalDate startDate, LocalDate endDate);

    /**
     * Annule ce qui est encore à venir : la création différée (le compte est alors supprimé) ou, si le
     * compte existe déjà, sa suppression planifiée.
     */
    void cancelPending(String identifier);

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
