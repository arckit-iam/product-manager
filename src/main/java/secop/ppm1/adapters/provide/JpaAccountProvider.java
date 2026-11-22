// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.AccountSource;
import secop.ppm1.domain.port.AccountProvider;

@Repository
public class JpaAccountProvider implements AccountProvider {

    private final JpaAccountRepository accountRepository;
    private final JpaProductRepository productRepository;

    @Autowired
    public JpaAccountProvider(JpaAccountRepository accountRepository, JpaProductRepository productRepository) {
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountRepository.findByIdentifier(account.getIdentifier())
                .orElseGet(AccountEntity::new);
        entity.setIdentifier(account.getIdentifier());
        entity.setType(account.getType());
        entity.setProductId(resolveProductId(account.getProduct()));
        entity.setSource(account.getSource().name());
        entity.setMetadata(account.getMetadata() == null ? new HashMap<>() : new HashMap<>(account.getMetadata()));
        entity.setJobId(account.getJobId());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(account.getCreatedAt() != null ? account.getCreatedAt() : OffsetDateTime.now());
        }
        return toDomain(accountRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Account> findByIdentifier(String identifier) {
        return accountRepository.findByIdentifier(identifier).map(this::toDomain);
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return accountRepository.existsByIdentifier(identifier);
    }

    @Override
    public List<Account> findAll(String productName, String typeName, boolean uncorrelatedOnly) {
        Specification<AccountEntity> spec = (root, query, cb) -> cb.conjunction();
        if (productName != null) {
            ProductEntity product = productRepository.findByName(productName);
            if (product == null) {
                return List.of();
            }
            spec = spec.and((root, query, cb) -> cb.equal(root.get("productId"), product.getId()));
        }
        if (typeName != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), typeName));
        }
        if (uncorrelatedOnly) {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("productId")));
        }
        return accountRepository.findAll(spec, Sort.by("identifier")).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteByIdentifier(String identifier) {
        accountRepository.deleteByIdentifier(identifier);
    }

    private Long resolveProductId(String productName) {
        if (productName == null) {
            return null;
        }
        ProductEntity product = productRepository.findByName(productName);
        if (product == null) {
            throw new NotFoundException("The product does not exist: " + productName);
        }
        return product.getId();
    }

    private Account toDomain(AccountEntity entity) {
        Account account = new Account();
        account.setId(entity.getId());
        account.setIdentifier(entity.getIdentifier());
        account.setType(entity.getType());
        account.setProduct(entity.getProductId() == null ? null
                : productRepository.findById(entity.getProductId()).map(ProductEntity::getName).orElse(null));
        account.setSource(AccountSource.valueOf(entity.getSource()));
        account.setMetadata(entity.getMetadata() == null ? new HashMap<>() : new HashMap<>(entity.getMetadata()));
        account.setJobId(entity.getJobId());
        account.setCreatedAt(entity.getCreatedAt());
        return account;
    }
}
