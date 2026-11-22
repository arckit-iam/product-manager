// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import secop.ppm1.domain.exception.AccountConflictException;
import secop.ppm1.domain.exception.InvalidAccountException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.AccountKind;
import secop.ppm1.domain.model.AccountSource;
import secop.ppm1.domain.model.AccountType;
import secop.ppm1.domain.model.MetadataField;
import secop.ppm1.domain.model.MetadataType;
import secop.ppm1.domain.port.AccountProvider;
import secop.ppm1.domain.port.AccountTypeCatalog;
import secop.ppm1.domain.port.JobPublisher;
import secop.ppm1.domain.port.ProductProvider;

class HexaAccountServiceTest {

    private final UUID jobId = UUID.randomUUID();

    private AccountProvider accounts;
    private ProductProvider products;
    private JobPublisher publisher;
    private HexaAccountService service;

    private final AccountType serviceType = new AccountType("service-account", AccountKind.NON_USER,
            Pattern.compile("svc-(?<product>[a-z0-9]+)-[a-z]+"),
            List.of(new MetadataField("owner", MetadataType.STRING, true)));
    private final AccountType employeeType = new AccountType("employee", AccountKind.USER,
            Pattern.compile("(?<identity>[a-z]+)"), List.of());

    @BeforeEach
    void setUp() {
        accounts = mock(AccountProvider.class);
        products = mock(ProductProvider.class);
        publisher = mock(JobPublisher.class);
        AccountTypeCatalog catalog = new AccountTypeCatalog() {
            @Override
            public List<AccountType> findAll() {
                return List.of(employeeType, serviceType);
            }

            @Override
            public Optional<AccountType> findByName(String name) {
                return findAll().stream().filter(t -> t.name().equals(name)).findFirst();
            }
        };
        service = new HexaAccountService(accounts, products, catalog, publisher);
        when(accounts.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(publisher.publishCreateAccount(any(Account.class))).thenReturn(jobId);
        when(products.existeByName("compta")).thenReturn(true);
    }

    private Account stored(String identifier, String product) {
        Account account = new Account();
        account.setIdentifier(identifier);
        account.setType("service-account");
        account.setProduct(product);
        account.setSource(AccountSource.DISCOVERED);
        when(accounts.findByIdentifier(identifier)).thenReturn(Optional.of(account));
        return account;
    }

    @Test
    void onlyNonUserTypesAreExposed() {
        assertEquals(List.of(serviceType), service.getAccountTypes());
    }

    @Test
    void listsAndFetchesAccounts() {
        Account account = stored("svc-compta-batch", "compta");
        when(accounts.findAll("compta", null, false)).thenReturn(List.of(account));

        assertEquals(List.of(account), service.getAccounts("compta", null, false));
        assertEquals(account, service.getAccount("svc-compta-batch"));
        assertThrows(NotFoundException.class, () -> service.getAccount("absent"));
    }

    @Test
    void createsAnAccountDeducingTypeAndProductFromTheRegex() {
        Account created = service.createAccount("svc-compta-batch", null, null, Map.of("owner", "alice"));

        assertEquals("service-account", created.getType());
        assertEquals("compta", created.getProduct());
        assertEquals(AccountSource.API, created.getSource());
        assertEquals(jobId, created.getJobId());
        verify(publisher).publishCreateAccount(any(Account.class));
    }

    @Test
    void explicitProductAndTypeWin() {
        when(products.existeByName("autre")).thenReturn(true);

        Account created = service.createAccount("svc-compta-batch", "service-account", "autre", Map.of("owner", "a"));

        assertEquals("autre", created.getProduct());
    }

    @Test
    void createRejectsInconsistentRequests() {
        assertThrows(InvalidAccountException.class, () -> service.createAccount(" ", null, null, Map.of()));
        assertThrows(InvalidAccountException.class,
                () -> service.createAccount("zzz", null, "compta", Map.of("owner", "a")));
        assertThrows(InvalidAccountException.class,
                () -> service.createAccount("svc-compta-batch", "nope", null, Map.of("owner", "a")));
        assertThrows(InvalidAccountException.class,
                () -> service.createAccount("alice", "employee", null, Map.of()));
        assertThrows(InvalidAccountException.class,
                () -> service.createAccount("zzz", "service-account", "compta", Map.of("owner", "a")));
        assertThrows(InvalidAccountException.class,
                () -> service.createAccount("svc-compta-batch", null, null, Map.of()));
        verify(publisher, never()).publishCreateAccount(any());
    }

    @Test
    void createNeedsAnExistingProduct() {
        AccountType free = new AccountType("free", AccountKind.NON_USER, Pattern.compile("free-.*"), List.of());
        HexaAccountService other = new HexaAccountService(accounts, products, new AccountTypeCatalog() {
            @Override
            public List<AccountType> findAll() {
                return List.of(free, serviceType);
            }

            @Override
            public Optional<AccountType> findByName(String name) {
                return Optional.empty();
            }
        }, publisher);

        assertThrows(InvalidAccountException.class, () -> other.createAccount("free-x", null, null, null));
        assertThrows(NotFoundException.class, () -> other.createAccount("svc-ghost-batch", null, null, Map.of("owner", "a")));
        assertThrows(NotFoundException.class, () -> other.createAccount("free-x", null, "ghost", null));
    }

    @Test
    void createRejectsDuplicates() {
        when(accounts.existsByIdentifier("svc-compta-batch")).thenReturn(true);

        assertThrows(AccountConflictException.class,
                () -> service.createAccount("svc-compta-batch", null, null, Map.of("owner", "a")));
    }

    @Test
    void updatesMetadataAfterValidation() {
        Account account = stored("svc-compta-batch", "compta");

        Account updated = service.updateMetadata("svc-compta-batch", Map.of("owner", "bob"));

        assertEquals(Map.of("owner", "bob"), updated.getMetadata());
        assertEquals(account, updated);
        assertThrows(InvalidAccountException.class, () -> service.updateMetadata("svc-compta-batch", Map.of()));
        assertThrows(InvalidAccountException.class, () -> service.updateMetadata("svc-compta-batch", Map.of("owner", 1)));
    }

    @Test
    void updateMetadataFailsWhenTheTypeIsNoLongerConfigured() {
        Account account = stored("old-account", null);
        account.setType("removed");

        assertThrows(InvalidAccountException.class, () -> service.updateMetadata("old-account", Map.of()));
    }

    @Test
    void correlatesManually() {
        stored("svc-x-batch", null);

        assertEquals("compta", service.correlate("svc-x-batch", "compta").getProduct());
        assertThrows(NotFoundException.class, () -> service.correlate("svc-x-batch", "ghost"));
        assertThrows(NotFoundException.class, () -> service.correlate("svc-x-batch", null));
        assertThrows(NotFoundException.class, () -> service.correlate("absent", "compta"));
    }

    @Test
    void correlatesAllUncorrelatedAccountsByRegex() {
        Account matching = stored("svc-compta-batch", null);
        Account unknownProduct = stored("svc-ghost-batch", null);
        Account unknownType = stored("legacy", null);
        unknownType.setType("removed");
        when(accounts.findAll(null, null, true)).thenReturn(List.of(matching, unknownProduct, unknownType));

        assertEquals(1, service.correlateAll());
        assertEquals("compta", matching.getProduct());
        assertNull(unknownProduct.getProduct());
    }

    @Test
    void deleteDeprovisionsThenRemoves() {
        Account account = stored("svc-compta-batch", "compta");

        service.deleteAccount("svc-compta-batch");

        verify(publisher).publishDeleteAccount(account);
        verify(accounts).deleteByIdentifier("svc-compta-batch");
        assertThrows(NotFoundException.class, () -> service.deleteAccount("absent"));
    }

    @Test
    void discoversNonUserAccounts() {
        service.discover("svc-compta-batch", "CREATED");
        service.discover("svc-ghost-batch", "CREATED");

        ArgumentCaptor<Account> saved = ArgumentCaptor.forClass(Account.class);
        verify(accounts, org.mockito.Mockito.times(2)).save(saved.capture());
        assertEquals("compta", saved.getAllValues().get(0).getProduct());
        assertNull(saved.getAllValues().get(1).getProduct());
        assertEquals(AccountSource.DISCOVERED, saved.getAllValues().get(0).getSource());
        verify(publisher, never()).publishCreateAccount(any());
    }

    @Test
    void discoveryIgnoresUsersKnownAccountsAndBlankIds() {
        when(accounts.existsByIdentifier("svc-compta-batch")).thenReturn(true);

        service.discover("alice", "CREATED");
        service.discover("svc-compta-batch", "MODIFIED");
        service.discover(" ", "CREATED");
        service.discover(null, "CREATED");

        verify(accounts, never()).save(any());
    }

    @Test
    void discoveryRemovesDeletedAccounts() {
        service.discover("svc-compta-batch", "deleted");

        verify(accounts).deleteByIdentifier(anyString());
    }
}
