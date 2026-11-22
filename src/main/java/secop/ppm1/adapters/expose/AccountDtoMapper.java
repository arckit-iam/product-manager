// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import org.springframework.stereotype.Component;

import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.AccountType;

@Component
public class AccountDtoMapper {

    public AccountDto toDto(Account account) {
        return new AccountDto(account.getIdentifier(), account.getType(), account.getProduct(),
                account.getSource() == null ? null : account.getSource().name(), account.getMetadata(),
                account.getStartDate(), account.getEndDate(), account.getOrderId(), account.getEndOrderId(),
                account.getCreatedAt());
    }

    public AccountTypeDto toDto(AccountType type) {
        return new AccountTypeDto(type.name(), type.kind().name(), type.pattern().pattern(),
                type.metadata().stream()
                        .map(f -> new AccountTypeDto.MetadataFieldDto(f.name(), f.type().name(), f.required()))
                        .toList());
    }
}
