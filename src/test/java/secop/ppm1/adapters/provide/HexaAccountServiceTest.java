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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import secop.ppm1.domain.model.ProvisioningOrder;
import secop.ppm1.domain.port.AccountProvider;
import secop.ppm1.domain.port.AccountTypeCatalog;
import secop.ppm1.domain.port.OrderPublisher;
import secop.ppm1.domain.port.ProductProvider;
import secop.ppm1.domain.service.OrderSchedule;

class HexaAccountServiceTest {

    private AccountProvider accounts;
    private ProductProvider products;
    private OrderPublisher publisher;
    private HexaAccountService service;

    private final AccountType serviceType = new AccountType("service-account", AccountKind.NON_USER,
            Pattern.compile("svc-(?<product>[a-z0-9]+)-[a-z]+"),
            List.of(new MetadataField("owner", MetadataType.STRING, true)));
    private final AccountType employeeType = new AccountType("employee", AccountKind.USER,
            Pattern.compile("(?<identity>[a-z]+)"), List.of());

    private final OrderSchedule schedule = new OrderSchedule(
            Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        accounts = mock(AccountProvider.class);
        products = mock(ProductProvider.class);
        publisher = mock(OrderPublisher.class);
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
        service = new HexaAccountService(accounts, products, catalog, publisher, schedule);
        when(accounts.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
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
        ArgumentCaptor<ProvisioningOrder> order = ArgumentCaptor.forClass(ProvisioningOrder.class);
        verify(publisher).submit(order.capture());
        assertEquals(order.getValue().id(), created.getOrderId());
        assertEquals(ProvisioningOrder.Action.CREATE, order.getValue().action());
        assertEquals(ProvisioningOrder.Target.IDENTITY, order.getValue().target());
        assertEquals("account:svc-compta-batch", order.getValue().correlationId());
        assertEquals(Instant.parse("2026-06-15T10:00:00Z"), order.getValue().executeAt().toInstant());
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
        verify(publisher, never()).submit(any());
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
        }, publisher, schedule);

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
    void deleteOrdersTheDeletionThenRemoves() {
        Account account = stored("svc-compta-batch", "compta");
        UUID endOrder = UUID.randomUUID();
        account.setEndOrderId(endOrder);

        service.deleteAccount("svc-compta-batch");

        verify(publisher).cancel(endOrder);
        ArgumentCaptor<ProvisioningOrder> order = ArgumentCaptor.forClass(ProvisioningOrder.class);
        verify(publisher).submit(order.capture());
        assertEquals(ProvisioningOrder.Action.DELETE, order.getValue().action());
        assertEquals("account:svc-compta-batch:delete", order.getValue().correlationId());
        verify(accounts).deleteByIdentifier("svc-compta-batch");
        assertThrows(NotFoundException.class, () -> service.deleteAccount("absent"));
    }

    @Test
    void deleteOfAnAccountNotYetCreatedOnlyCancelsItsOrder() {
        Account account = stored("svc-compta-batch", "compta");
        UUID createOrder = UUID.randomUUID();
        account.setStartDate(LocalDate.of(2026, 7, 1));
        account.setOrderId(createOrder);

        service.deleteAccount("svc-compta-batch");

        verify(publisher).cancel(createOrder);
        verify(publisher, never()).submit(any());
        verify(accounts).deleteByIdentifier("svc-compta-batch");
    }

    @Test
    void deleteOfAnExpiredAccountOrdersNothing() {
        Account account = stored("svc-compta-batch", "compta");
        account.setEndDate(LocalDate.of(2026, 6, 1));

        service.deleteAccount("svc-compta-batch");

        verify(publisher, never()).submit(any());
        verify(accounts).deleteByIdentifier("svc-compta-batch");
    }

    @Test
    void createSchedulesTheCreationAndTheEnd() {
        Account created = service.createAccount("svc-compta-batch", null, null, Map.of("owner", "a"),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        ArgumentCaptor<ProvisioningOrder> orders = ArgumentCaptor.forClass(ProvisioningOrder.class);
        verify(publisher, org.mockito.Mockito.times(2)).submit(orders.capture());
        ProvisioningOrder create = orders.getAllValues().get(0);
        ProvisioningOrder end = orders.getAllValues().get(1);
        assertEquals(Instant.parse("2026-07-01T00:00:00Z"), create.executeAt().toInstant());
        assertEquals(Instant.parse("2026-08-01T00:00:00Z"), end.executeAt().toInstant());
        assertEquals("account:svc-compta-batch:end", end.correlationId());
        assertEquals(create.id(), created.getOrderId());
        assertEquals(end.id(), created.getEndOrderId());
    }

    @Test
    void createRejectsInconsistentDates() {
        assertThrows(InvalidAccountException.class, () -> service.createAccount("svc-compta-batch", null, null,
                Map.of("owner", "a"), LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 1)));
        assertThrows(InvalidAccountException.class, () -> service.createAccount("svc-compta-batch", null, null,
                Map.of("owner", "a"), null, LocalDate.of(2026, 6, 14)));
        verify(publisher, never()).submit(any());
    }

    @Test
    void createCancelsTheCreationWhenTheEndCannotBeScheduled() {
        org.mockito.Mockito.doNothing().doThrow(new IllegalStateException("down")).when(publisher)
                .submit(any(ProvisioningOrder.class));

        assertThrows(IllegalStateException.class, () -> service.createAccount("svc-compta-batch", null, null,
                Map.of("owner", "a"), null, LocalDate.of(2026, 7, 31)));

        verify(publisher).cancel(any(UUID.class));
        verify(accounts, never()).save(any());
    }

    @Test
    void updateDatesOfAPendingAccountReplacesItsOrders() {
        Account account = stored("svc-compta-batch", "compta");
        UUID oldCreate = UUID.randomUUID();
        UUID oldEnd = UUID.randomUUID();
        account.setStartDate(LocalDate.of(2026, 7, 1));
        account.setEndDate(LocalDate.of(2026, 7, 31));
        account.setOrderId(oldCreate);
        account.setEndOrderId(oldEnd);

        service.updateDates("svc-compta-batch", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        verify(publisher).cancel(oldCreate);
        verify(publisher).cancel(oldEnd);
        verify(publisher, org.mockito.Mockito.times(2)).submit(any(ProvisioningOrder.class));
        assertEquals(LocalDate.of(2026, 8, 1), account.getStartDate());
    }

    @Test
    void updateDatesCanRemoveTheEnd() {
        Account account = stored("svc-compta-batch", "compta");
        UUID oldEnd = UUID.randomUUID();
        account.setEndDate(LocalDate.of(2026, 7, 31));
        account.setEndOrderId(oldEnd);

        service.updateDates("svc-compta-batch", null, null);

        verify(publisher).cancel(oldEnd);
        verify(publisher, never()).submit(any());
        assertNull(account.getEndOrderId());
    }

    @Test
    void updateDatesRefusesAStartAlreadyReachedOrAnExpiredAccount() {
        Account account = stored("svc-compta-batch", "compta");
        account.setStartDate(LocalDate.of(2026, 6, 1));
        assertThrows(InvalidAccountException.class,
                () -> service.updateDates("svc-compta-batch", LocalDate.of(2026, 6, 20), null));

        account.setEndDate(LocalDate.of(2026, 6, 10));
        assertThrows(AccountConflictException.class,
                () -> service.updateDates("svc-compta-batch", LocalDate.of(2026, 6, 1), null));
    }

    @Test
    void cancelPendingCreationRemovesTheAccount() {
        Account account = stored("svc-compta-batch", "compta");
        UUID createOrder = UUID.randomUUID();
        UUID endOrder = UUID.randomUUID();
        account.setStartDate(LocalDate.of(2026, 7, 1));
        account.setOrderId(createOrder);
        account.setEndOrderId(endOrder);

        service.cancelPending("svc-compta-batch");

        verify(publisher).cancel(createOrder);
        verify(publisher).cancel(endOrder);
        verify(accounts).deleteByIdentifier("svc-compta-batch");
    }

    @Test
    void cancelPendingEndKeepsTheAccount() {
        Account account = stored("svc-compta-batch", "compta");
        UUID endOrder = UUID.randomUUID();
        account.setEndDate(LocalDate.of(2026, 7, 31));
        account.setEndOrderId(endOrder);

        service.cancelPending("svc-compta-batch");

        verify(publisher).cancel(endOrder);
        assertNull(account.getEndDate());
        assertNull(account.getEndOrderId());
        verify(accounts).save(account);
        verify(accounts, never()).deleteByIdentifier(anyString());
    }

    @Test
    void cancelPendingWithNothingPendingConflicts() {
        stored("svc-compta-batch", "compta");

        assertThrows(AccountConflictException.class, () -> service.cancelPending("svc-compta-batch"));
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
        verify(publisher, never()).submit(any());
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
