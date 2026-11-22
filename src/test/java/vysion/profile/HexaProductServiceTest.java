// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import secop.ppm1.adapters.provide.HexaProductService;
import secop.ppm1.domain.exception.AccountConflictException;
import secop.ppm1.domain.exception.ExistProductNameConstraintViolationException;
import secop.ppm1.domain.exception.InvalidAccountException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.ProvisioningOrder;
import secop.ppm1.domain.port.OrderPublisher;
import secop.ppm1.domain.port.ProductProvider;
import secop.ppm1.domain.port.ProfileProvider;
import secop.ppm1.domain.service.OrderSchedule;

public class HexaProductServiceTest {

    @Mock
    private ProductProvider productProvider;

    @Mock
    private ProfileProvider profileProvider;

    @Mock
    private OrderPublisher orderPublisher;

    private HexaProductService hexaProductService;

    private Product testProduct;
    private Profile testProfile;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        OrderSchedule schedule = new OrderSchedule(
                Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC), ZoneOffset.UTC);
        hexaProductService = new HexaProductService(productProvider, profileProvider, orderPublisher, schedule);
        
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");

        testProfile = new Profile();
        testProfile.setName("Test Profile");
        testProfile.setProduct("Test Product");
        testProfile.setDescription("Test Profile Description");
        testProfile.setScope("TIER1");
    }

    @Test
    public void testGetAllProducts() {
        Product product1 = new Product();
        product1.setName("Product1");
        Product product2 = new Product();
        product2.setName("Product2");
        
        List<Product> products = Arrays.asList(product1, product2);
        when(productProvider.findAll()).thenReturn(products);

        List<Product> result = hexaProductService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Product1", result.get(0).getName());
        assertEquals("Product2", result.get(1).getName());
        verify(productProvider, times(1)).findAll();
    }

    @Test
    public void testGetProductById() {
        when(productProvider.findById(1L)).thenReturn(testProduct);

        Product result = hexaProductService.getProduct(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productProvider, times(1)).findById(1L);
    }

    @Test
    public void testGetProductByName() {
        when(productProvider.findByProductName("Test Product")).thenReturn(testProduct);

        Product result = hexaProductService.getProduct("Test Product");

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productProvider, times(1)).findByProductName("Test Product");
    }

    @Test
    public void testCreateProduct_Success() {
        when(productProvider.existeByName("Test Product")).thenReturn(false);
        when(productProvider.save(testProduct)).thenReturn(testProduct);

        Product result = hexaProductService.createProduct(testProduct);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productProvider, times(1)).existeByName("Test Product");
        verify(productProvider, times(1)).save(testProduct);
    }

    @Test
    public void testCreateProduct_ThrowsUniqueConstraintException() {
        when(productProvider.existeByName("Test Product")).thenReturn(true);

        assertThrows(UniqueProductNameConstraintViolationException.class, () -> {
            hexaProductService.createProduct(testProduct);
        });

        verify(productProvider, times(1)).existeByName("Test Product");
        verify(productProvider, never()).save(any(Product.class));
    }

    @Test
    public void testDeleteProductById() {
        doNothing().when(productProvider).deleteById(1L);

        hexaProductService.deleteProduct(1L);

        verify(productProvider, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteProductByName_Success() {
        when(productProvider.existeByName("Test Product")).thenReturn(true);
        doNothing().when(productProvider).deleteByName("Test Product");

        hexaProductService.deleteProduct("Test Product");

        verify(productProvider, times(1)).existeByName("Test Product");
        verify(productProvider, times(1)).deleteByName("Test Product");
    }

    @Test
    public void testDeleteProductByName_ThrowsNotFoundException() {
        when(productProvider.existeByName("Non Existing Product")).thenReturn(false);

        assertThrows(NotFoundException.class, () -> {
            hexaProductService.deleteProduct("Non Existing Product");
        });

        verify(productProvider, times(1)).existeByName("Non Existing Product");
        verify(productProvider, never()).deleteByName(anyString());
    }

    @Test
    public void testUpdateProduct_Success() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Test Product");
        updatedProduct.setDescription("Updated Description");

        when(productProvider.existeByName("Test Product")).thenReturn(true);
        when(productProvider.save(updatedProduct)).thenReturn(updatedProduct);

        Product result = hexaProductService.updateProduct("Test Product", updatedProduct);

        assertNotNull(result);
        assertEquals("Updated Description", result.getDescription());
        verify(productProvider, times(2)).existeByName("Test Product");
        verify(productProvider, times(1)).save(updatedProduct);
    }

    @Test
    public void testUpdateProduct_ThrowsNotFoundExceptionWhenProductNotExists() {
        when(productProvider.existeByName("Non Existing Product")).thenReturn(false);

        assertThrows(NotFoundException.class, () -> {
            hexaProductService.updateProduct("Non Existing Product", testProduct);
        });

        verify(productProvider, times(1)).existeByName("Non Existing Product");
        verify(productProvider, never()).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_ThrowsExistConstraintExceptionWhenNewNameNotExists() {
        Product updatedProduct = new Product();
        updatedProduct.setName("New Product Name");

        when(productProvider.existeByName("Test Product")).thenReturn(true);
        when(productProvider.existeByName("New Product Name")).thenReturn(false);

        assertThrows(ExistProductNameConstraintViolationException.class, () -> {
            hexaProductService.updateProduct("Test Product", updatedProduct);
        });

        verify(productProvider, times(1)).existeByName("Test Product");
        verify(productProvider, times(1)).existeByName("New Product Name");
        verify(productProvider, never()).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_ThrowsUniqueConstraintExceptionWhenChangingName() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Different Name");

        when(productProvider.existeByName("Test Product")).thenReturn(true);
        when(productProvider.existeByName("Different Name")).thenReturn(true);

        assertThrows(UniqueProductNameConstraintViolationException.class, () -> {
            hexaProductService.updateProduct("Test Product", updatedProduct);
        });

        verify(productProvider, times(1)).existeByName("Test Product");
        verify(productProvider, times(1)).existeByName("Different Name");
        verify(productProvider, never()).save(any(Product.class));
    }

    @Test
    public void testAddProfileToProduct() {
        List<Profile> profiles = Arrays.asList(testProfile);
        testProduct.setProfiles(profiles);

        when(profileProvider.save(testProfile, "Test Product")).thenReturn(testProfile);
        when(productProvider.findByProductName("Test Product")).thenReturn(testProduct);
        when(profileProvider.getProfilesByProduct("Test Product")).thenReturn(profiles);

        Product result = hexaProductService.addProfileToProduct("Test Product", testProfile);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(1, result.getProfiles().size());
        ArgumentCaptor<ProvisioningOrder> order = ArgumentCaptor.forClass(ProvisioningOrder.class);
        verify(orderPublisher, times(1)).submit(order.capture());
        assertEquals(ProvisioningOrder.Action.CREATE, order.getValue().action());
        assertEquals(ProvisioningOrder.Target.ENTITLEMENT, order.getValue().target());
        assertEquals("profile:Test Product:Test Profile", order.getValue().correlationId());
        assertEquals(order.getValue().id(), testProfile.getOrderId());
        assertNull(testProfile.getEndOrderId());
        verify(profileProvider, times(1)).save(testProfile, "Test Product");
    }

    @Test
    public void testAddProfileSchedulesTheCreationAndTheEnd() {
        testProfile.setStartDate(LocalDate.of(2026, 7, 1));
        testProfile.setEndDate(LocalDate.of(2026, 7, 31));
        when(productProvider.findByProductName("Test Product")).thenReturn(testProduct);

        hexaProductService.addProfileToProduct("Test Product", testProfile);

        ArgumentCaptor<ProvisioningOrder> orders = ArgumentCaptor.forClass(ProvisioningOrder.class);
        verify(orderPublisher, times(2)).submit(orders.capture());
        ProvisioningOrder create = orders.getAllValues().get(0);
        ProvisioningOrder end = orders.getAllValues().get(1);
        assertEquals(Instant.parse("2026-07-01T00:00:00Z"), create.executeAt().toInstant());
        assertEquals(Instant.parse("2026-08-01T00:00:00Z"), end.executeAt().toInstant());
        assertEquals(ProvisioningOrder.Action.DELETE, end.action());
        assertEquals("profile:Test Product:Test Profile:end", end.correlationId());
        assertEquals(create.id(), testProfile.getOrderId());
        assertEquals(end.id(), testProfile.getEndOrderId());
    }

    @Test
    public void testAddProfileRejectsInconsistentDates() {
        testProfile.setStartDate(LocalDate.of(2026, 7, 10));
        testProfile.setEndDate(LocalDate.of(2026, 7, 1));
        assertThrows(InvalidAccountException.class,
                () -> hexaProductService.addProfileToProduct("Test Product", testProfile));

        testProfile.setStartDate(null);
        testProfile.setEndDate(LocalDate.of(2026, 6, 14));
        assertThrows(InvalidAccountException.class,
                () -> hexaProductService.addProfileToProduct("Test Product", testProfile));

        verify(orderPublisher, never()).submit(any());
    }

    @Test
    public void testAddProfileCancelsTheCreationWhenTheEndCannotBeScheduled() {
        testProfile.setEndDate(LocalDate.of(2026, 7, 31));
        org.mockito.Mockito.doNothing().doThrow(new IllegalStateException("down")).when(orderPublisher)
                .submit(any(ProvisioningOrder.class));

        assertThrows(IllegalStateException.class,
                () -> hexaProductService.addProfileToProduct("Test Product", testProfile));

        verify(orderPublisher).cancel(testProfile.getOrderId());
        verify(profileProvider, never()).save(any(Profile.class), anyString());
    }

    private void stored(Profile profile) {
        when(productProvider.existeByName("Test Product")).thenReturn(true);
        when(profileProvider.getProfileByNameAndProduct(profile.getName(), "Test Product")).thenReturn(profile);
        when(productProvider.findByProductName("Test Product")).thenReturn(testProduct);
        when(profileProvider.getProfilesByProduct("Test Product")).thenReturn(List.of());
    }

    @Test
    public void testDeleteActiveProfileOrdersItsDeletionAndCancelsTheEnd() {
        UUID endOrder = UUID.randomUUID();
        testProfile.setEndOrderId(endOrder);
        stored(testProfile);

        Product result = hexaProductService.deleteProfileToProduct("Test Product", "Test Profile");

        assertNotNull(result);
        verify(orderPublisher).cancel(endOrder);
        ArgumentCaptor<ProvisioningOrder> order = ArgumentCaptor.forClass(ProvisioningOrder.class);
        verify(orderPublisher).submit(order.capture());
        assertEquals(ProvisioningOrder.Action.DELETE, order.getValue().action());
        assertEquals("profile:Test Product:Test Profile:delete", order.getValue().correlationId());
        verify(profileProvider).deleteProfile("Test Product", "Test Profile");
    }

    @Test
    public void testDeleteProfileNotYetCreatedOnlyCancelsItsOrder() {
        UUID createOrder = UUID.randomUUID();
        testProfile.setStartDate(LocalDate.of(2026, 7, 1));
        testProfile.setOrderId(createOrder);
        stored(testProfile);

        hexaProductService.deleteProfileToProduct("Test Product", "Test Profile");

        verify(orderPublisher).cancel(createOrder);
        verify(orderPublisher, never()).submit(any());
        verify(profileProvider).deleteProfile("Test Product", "Test Profile");
    }

    @Test
    public void testDeleteExpiredProfileOrdersNothing() {
        testProfile.setEndDate(LocalDate.of(2026, 6, 1));
        stored(testProfile);

        hexaProductService.deleteProfileToProduct("Test Product", "Test Profile");

        verify(orderPublisher, never()).submit(any());
        verify(profileProvider).deleteProfile("Test Product", "Test Profile");
    }

    @Test
    public void testDeleteUnknownProfileOrProduct() {
        when(productProvider.existeByName("Test Product")).thenReturn(true);
        assertThrows(NotFoundException.class,
                () -> hexaProductService.deleteProfileToProduct("Test Product", "absent"));
        assertThrows(NotFoundException.class,
                () -> hexaProductService.deleteProfileToProduct("Ghost", "Test Profile"));
    }

    @Test
    public void testUpdateDatesOfAPendingProfileReplacesItsOrders() {
        UUID oldCreate = UUID.randomUUID();
        UUID oldEnd = UUID.randomUUID();
        testProfile.setStartDate(LocalDate.of(2026, 7, 1));
        testProfile.setEndDate(LocalDate.of(2026, 7, 31));
        testProfile.setOrderId(oldCreate);
        testProfile.setEndOrderId(oldEnd);
        stored(testProfile);

        hexaProductService.updateProfileDates("Test Product", "Test Profile", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31));

        verify(orderPublisher).cancel(oldCreate);
        verify(orderPublisher).cancel(oldEnd);
        verify(orderPublisher, times(2)).submit(any(ProvisioningOrder.class));
        assertEquals(LocalDate.of(2026, 8, 1), testProfile.getStartDate());
        verify(profileProvider).save(testProfile, "Test Product");
    }

    @Test
    public void testUpdateDatesRemovesTheEnd() {
        UUID oldEnd = UUID.randomUUID();
        testProfile.setEndDate(LocalDate.of(2026, 7, 31));
        testProfile.setEndOrderId(oldEnd);
        stored(testProfile);

        hexaProductService.updateProfileDates("Test Product", "Test Profile", null, null);

        verify(orderPublisher).cancel(oldEnd);
        verify(orderPublisher, never()).submit(any());
        assertNull(testProfile.getEndOrderId());
        assertNull(testProfile.getEndDate());
    }

    @Test
    public void testUpdateDatesRefusesToMoveAStartThatIsAlreadyReachedOrAnExpiredProfile() {
        testProfile.setStartDate(LocalDate.of(2026, 6, 1));
        stored(testProfile);
        assertThrows(InvalidAccountException.class, () -> hexaProductService.updateProfileDates("Test Product",
                "Test Profile", LocalDate.of(2026, 6, 20), null));

        testProfile.setEndDate(LocalDate.of(2026, 6, 10));
        assertThrows(AccountConflictException.class, () -> hexaProductService.updateProfileDates("Test Product",
                "Test Profile", LocalDate.of(2026, 6, 1), null));
    }

    @Test
    public void testCancelPendingCreationDeletesTheProfile() {
        UUID createOrder = UUID.randomUUID();
        UUID endOrder = UUID.randomUUID();
        testProfile.setStartDate(LocalDate.of(2026, 7, 1));
        testProfile.setEndDate(LocalDate.of(2026, 7, 31));
        testProfile.setOrderId(createOrder);
        testProfile.setEndOrderId(endOrder);
        stored(testProfile);

        hexaProductService.cancelProfilePending("Test Product", "Test Profile");

        verify(orderPublisher).cancel(createOrder);
        verify(orderPublisher).cancel(endOrder);
        verify(profileProvider).deleteProfile("Test Product", "Test Profile");
    }

    @Test
    public void testCancelPendingEndKeepsTheProfile() {
        UUID endOrder = UUID.randomUUID();
        testProfile.setEndDate(LocalDate.of(2026, 7, 31));
        testProfile.setEndOrderId(endOrder);
        stored(testProfile);

        hexaProductService.cancelProfilePending("Test Product", "Test Profile");

        verify(orderPublisher).cancel(endOrder);
        assertNull(testProfile.getEndDate());
        assertNull(testProfile.getEndOrderId());
        verify(profileProvider).save(testProfile, "Test Product");
        verify(profileProvider, never()).deleteProfile(anyString(), anyString());
    }

    @Test
    public void testCancelPendingWithNothingPendingConflicts() {
        stored(testProfile);

        assertThrows(AccountConflictException.class,
                () -> hexaProductService.cancelProfilePending("Test Product", "Test Profile"));
    }

    @Test
    public void testAddResourceToProduct_ThrowsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            hexaProductService.addResourceToProduct("Test Product", null);
        });
    }

    @Test
    public void testUpdateProductById_ThrowsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            hexaProductService.updateProduct(1L, testProduct);
        });
    }
}