// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import secop.ppm1.domain.exception.AccountConflictException;
import secop.ppm1.domain.exception.InvalidAccountException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.AccountKind;
import secop.ppm1.domain.model.AccountMatch;
import secop.ppm1.domain.model.AccountSource;
import secop.ppm1.domain.model.AccountType;
import secop.ppm1.domain.port.AccountProvider;
import secop.ppm1.domain.port.AccountTypeCatalog;
import secop.ppm1.domain.port.JobPublisher;
import secop.ppm1.domain.port.ProductProvider;
import secop.ppm1.domain.service.AccountManager;

@Service
public class HexaAccountService implements AccountManager {

    private static final String DELETED = "DELETED";

    private final AccountProvider accountProvider;
    private final ProductProvider productProvider;
    private final AccountTypeCatalog catalog;
    private final JobPublisher jobPublisher;

    @Autowired
    HexaAccountService(AccountProvider accountProvider, ProductProvider productProvider,
            AccountTypeCatalog catalog, JobPublisher jobPublisher) {
        this.accountProvider = accountProvider;
        this.productProvider = productProvider;
        this.catalog = catalog;
        this.jobPublisher = jobPublisher;
    }

    @Override
    public List<AccountType> getAccountTypes() {
        return catalog.findAll().stream().filter(t -> t.kind() == AccountKind.NON_USER).toList();
    }

    @Override
    public List<Account> getAccounts(String productName, String typeName, boolean uncorrelatedOnly) {
        return accountProvider.findAll(productName, typeName, uncorrelatedOnly);
    }

    @Override
    public Account getAccount(String identifier) {
        return accountProvider.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundException("The account does not exist: " + identifier));
    }

    @Override
    public Account createAccount(String identifier, String typeName, String productName,
            Map<String, Object> metadata) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidAccountException("The account identifier is required");
        }
        if (accountProvider.existsByIdentifier(identifier)) {
            throw new AccountConflictException("The account already exists: " + identifier);
        }
        AccountMatch match = resolveType(identifier, typeName);
        String product = productName != null ? productName : match.product().orElse(null);
        if (product == null) {
            throw new InvalidAccountException("A product is required: none given and none captured by the type pattern");
        }
        if (!productProvider.existeByName(product)) {
            throw new NotFoundException("The product does not exist: " + product);
        }
        match.type().validateMetadata(metadata);

        Account account = new Account();
        account.setIdentifier(identifier);
        account.setType(match.type().name());
        account.setProduct(product);
        account.setSource(AccountSource.API);
        account.setMetadata(metadata == null ? new HashMap<>() : new HashMap<>(metadata));
        account.setJobId(jobPublisher.publishCreateAccount(account));
        account.setCreatedAt(OffsetDateTime.now());
        return accountProvider.save(account);
    }

    private AccountMatch resolveType(String identifier, String typeName) {
        if (typeName == null) {
            return catalog.classify(identifier, AccountKind.NON_USER).orElseThrow(
                    () -> new InvalidAccountException("No account type recognizes the identifier " + identifier));
        }
        AccountType type = catalog.findByName(typeName)
                .orElseThrow(() -> new InvalidAccountException("Unknown account type: " + typeName));
        if (type.kind() != AccountKind.NON_USER) {
            throw new InvalidAccountException("Type " + typeName + " is not a non-user account type");
        }
        return type.match(identifier).orElseThrow(
                () -> new InvalidAccountException("The identifier does not match the pattern of type " + typeName));
    }

    @Override
    public Account updateMetadata(String identifier, Map<String, Object> metadata) {
        Account account = getAccount(identifier);
        AccountType type = catalog.findByName(account.getType())
                .orElseThrow(() -> new InvalidAccountException("Unknown account type: " + account.getType()));
        type.validateMetadata(metadata);
        account.setMetadata(metadata == null ? new HashMap<>() : new HashMap<>(metadata));
        return accountProvider.save(account);
    }

    @Override
    public Account correlate(String identifier, String productName) {
        Account account = getAccount(identifier);
        if (productName == null || !productProvider.existeByName(productName)) {
            throw new NotFoundException("The product does not exist: " + productName);
        }
        account.setProduct(productName);
        return accountProvider.save(account);
    }

    @Override
    public int correlateAll() {
        int correlated = 0;
        for (Account account : accountProvider.findAll(null, null, true)) {
            Optional<String> product = catalog.findByName(account.getType())
                    .flatMap(type -> type.match(account.getIdentifier()))
                    .flatMap(AccountMatch::product)
                    .filter(productProvider::existeByName);
            if (product.isPresent()) {
                account.setProduct(product.get());
                accountProvider.save(account);
                correlated++;
            }
        }
        return correlated;
    }

    @Override
    public void deleteAccount(String identifier) {
        Account account = getAccount(identifier);
        jobPublisher.publishDeleteAccount(account);
        accountProvider.deleteByIdentifier(identifier);
    }

    @Override
    public void discover(String identifier, String eventType) {
        if (identifier == null || identifier.isBlank()) {
            return;
        }
        if (DELETED.equalsIgnoreCase(eventType)) {
            accountProvider.deleteByIdentifier(identifier);
            return;
        }
        if (accountProvider.existsByIdentifier(identifier)) {
            return;
        }
        catalog.classify(identifier, AccountKind.NON_USER).ifPresent(match -> {
            Account account = new Account();
            account.setIdentifier(identifier);
            account.setType(match.type().name());
            account.setProduct(match.product().filter(productProvider::existeByName).orElse(null));
            account.setSource(AccountSource.DISCOVERED);
            account.setCreatedAt(OffsetDateTime.now());
            accountProvider.save(account);
        });
    }
}
