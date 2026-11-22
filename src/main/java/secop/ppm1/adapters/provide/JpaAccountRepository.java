// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaAccountRepository
        extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    void deleteByIdentifier(String identifier);
}
