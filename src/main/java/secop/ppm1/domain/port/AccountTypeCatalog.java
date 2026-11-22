// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.port;

import java.util.List;
import java.util.Optional;

import secop.ppm1.domain.model.AccountKind;
import secop.ppm1.domain.model.AccountMatch;
import secop.ppm1.domain.model.AccountType;

/** Catalogue des types de compte (défini par la configuration du déploiement). */
public interface AccountTypeCatalog {

    List<AccountType> findAll();

    Optional<AccountType> findByName(String name);

    /** Premier type, dans l'ordre de configuration, de la nature demandée dont la regex reconnaît l'identifiant. */
    default Optional<AccountMatch> classify(String identifier, AccountKind kind) {
        return findAll().stream()
                .filter(type -> type.kind() == kind)
                .map(type -> type.match(identifier))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
