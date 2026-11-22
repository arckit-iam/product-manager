// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.port;

import java.util.List;
import java.util.Optional;

import secop.ppm1.domain.model.Account;

public interface AccountProvider {

    Account save(Account account);

    Optional<Account> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    /**
     * @param productName filtre sur le produit (ignoré si {@code null})
     * @param typeName filtre sur le type (ignoré si {@code null})
     * @param uncorrelatedOnly ne retourne que les comptes sans produit
     */
    List<Account> findAll(String productName, String typeName, boolean uncorrelatedOnly);

    void deleteByIdentifier(String identifier);
}
