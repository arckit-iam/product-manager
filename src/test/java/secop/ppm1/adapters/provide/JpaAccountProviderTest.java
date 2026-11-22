// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.AccountSource;

class JpaAccountProviderTest {

    private JpaAccountRepository accounts;
    private JpaProductRepository products;
    private JpaAccountProvider provider;

    @BeforeEach
    void setUp() {
        accounts = mock(JpaAccountRepository.class);
        products = mock(JpaProductRepository.class);
        provider = new JpaAccountProvider(accounts, products);
        when(accounts.saveAndFlush(any(AccountEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ProductEntity product(Long id, String name) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        when(products.findByName(name)).thenReturn(product);
        when(products.findById(id)).thenReturn(Optional.of(product));
        return product;
    }

    private Account account(String product) {
        Account account = new Account();
        account.setIdentifier("svc-compta-batch");
        account.setType("service-account");
        account.setProduct(product);
        account.setSource(AccountSource.API);
        account.setMetadata(new HashMap<>(Map.of("owner", "alice")));
        account.setOrderId(UUID.randomUUID());
        account.setEndOrderId(UUID.randomUUID());
        account.setStartDate(java.time.LocalDate.of(2026, 7, 1));
        account.setEndDate(java.time.LocalDate.of(2026, 7, 31));
        return account;
    }

    @Test
    void savesANewAccountLinkedToItsProduct() {
        product(7L, "compta");
        when(accounts.findByIdentifier("svc-compta-batch")).thenReturn(Optional.empty());

        Account saved = provider.save(account("compta"));

        assertEquals("compta", saved.getProduct());
        assertEquals(Map.of("owner", "alice"), saved.getMetadata());
        assertEquals(AccountSource.API, saved.getSource());
        assertEquals(java.time.LocalDate.of(2026, 7, 1), saved.getStartDate());
        assertEquals(java.time.LocalDate.of(2026, 7, 31), saved.getEndDate());
        assertTrue(saved.getOrderId() != null && saved.getEndOrderId() != null);
        assertTrue(saved.getCreatedAt() != null);
    }

    @Test
    void savesAnUncorrelatedAccountAndKeepsTheCreationDate() {
        AccountEntity existing = new AccountEntity();
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        existing.setCreatedAt(created);
        when(accounts.findByIdentifier("svc-compta-batch")).thenReturn(Optional.of(existing));
        Account input = account(null);
        input.setMetadata(null);

        Account saved = provider.save(input);

        assertNull(saved.getProduct());
        assertEquals(Map.of(), saved.getMetadata());
        assertEquals(created, saved.getCreatedAt());
    }

    @Test
    void usesTheDomainCreationDateForNewAccounts() {
        when(accounts.findByIdentifier("svc-compta-batch")).thenReturn(Optional.empty());
        Account input = account(null);
        OffsetDateTime created = OffsetDateTime.now().minusDays(2);
        input.setCreatedAt(created);

        assertEquals(created, provider.save(input).getCreatedAt());
    }

    @Test
    void failsOnAnUnknownProduct() {
        when(products.findByName("ghost")).thenReturn(null);
        when(accounts.findByIdentifier("svc-compta-batch")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> provider.save(account("ghost")));
    }

    @Test
    void readsAnAccountWithItsProductName() {
        product(7L, "compta");
        AccountEntity entity = new AccountEntity();
        entity.setIdentifier("svc-compta-batch");
        entity.setType("service-account");
        entity.setProductId(7L);
        entity.setSource("DISCOVERED");
        entity.setMetadata(null);
        when(accounts.findByIdentifier("svc-compta-batch")).thenReturn(Optional.of(entity));
        when(accounts.existsByIdentifier("svc-compta-batch")).thenReturn(true);

        Account found = provider.findByIdentifier("svc-compta-batch").orElseThrow();

        assertEquals("compta", found.getProduct());
        assertEquals(AccountSource.DISCOVERED, found.getSource());
        assertTrue(provider.existsByIdentifier("svc-compta-batch"));
        assertTrue(provider.findByIdentifier("absent").isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listsWithFilters() {
        product(7L, "compta");
        AccountEntity entity = new AccountEntity();
        entity.setIdentifier("a");
        entity.setSource("API");
        when(accounts.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(entity));

        assertEquals(1, provider.findAll("compta", "service-account", true).size());
        assertEquals(1, provider.findAll(null, null, false).size());
    }

    @Test
    void listingAnUnknownProductIsEmpty() {
        when(products.findByName("ghost")).thenReturn(null);

        assertEquals(List.of(), provider.findAll("ghost", null, false));
    }

    @Test
    void deletes() {
        provider.deleteByIdentifier("svc-compta-batch");

        verify(accounts).deleteByIdentifier("svc-compta-batch");
    }
}
