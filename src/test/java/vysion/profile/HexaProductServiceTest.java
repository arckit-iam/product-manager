package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import secop.ppm1.adapters.provide.HexaProductService;
import secop.ppm1.domain.exception.ExistProductNameConstraintViolationException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.port.ProductProvider;
import secop.ppm1.domain.port.ProfileProvider;

public class HexaProductServiceTest {

    @Mock
    private ProductProvider productProvider;

    @Mock
    private ProfileProvider profileProvider;

    @InjectMocks
    private HexaProductService hexaProductService;

    private Product testProduct;
    private Profile testProfile;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");

        testProfile = new Profile();
        testProfile.setName("Test Profile");
        testProfile.setProduct("Test Product");
        testProfile.setDescription("Test Profile Description");
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
        verify(profileProvider, times(1)).save(testProfile, "Test Product");
        verify(productProvider, times(1)).findByProductName("Test Product");
        verify(profileProvider, times(1)).getProfilesByProduct("Test Product");
    }

    @Test
    public void testDeleteProfileToProduct() {
        List<Profile> profiles = Arrays.asList(); // Liste vide après suppression
        testProduct.setProfiles(profiles);

        doNothing().when(profileProvider).deleteProfile(testProfile);
        when(productProvider.findByProductName("Test Product")).thenReturn(testProduct);
        when(profileProvider.getProfilesByProduct("Test Product")).thenReturn(profiles);

        Product result = hexaProductService.deleteProfileToProduct("Test Product", testProfile);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(0, result.getProfiles().size());
        verify(profileProvider, times(1)).deleteProfile(testProfile);
        verify(productProvider, times(1)).findByProductName("Test Product");
        verify(profileProvider, times(1)).getProfilesByProduct("Test Product");
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