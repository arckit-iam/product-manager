package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.port.ProductProvider;





public class ProductProviderTest {

    @Mock
    private ProductProvider productProvider;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindAll() {
        Product product1 = new Product();
        product1.setName("Product1");

        Product product2 = new Product();

        product2.setName("Product2");
        List<Product> products = Arrays.asList(product1, product2);

        when(productProvider.findAll()).thenReturn(products);

        List<Product> result = productProvider.findAll();
        assertEquals(2, result.size());
        assertEquals("Product1", result.get(0).getName());
        assertEquals("Product2", result.get(1).getName());
    }

    @Test
    public void testFindById() {
        Product product = new Product();
        product.setName("Product1");    
        when(productProvider.findById(1L)).thenReturn(product);

        Product result = productProvider.findById(1L);
        assertNotNull(result);
        assertEquals("Product1", result.getName());
    }

    @Test
    public void testSave() {
        Product product = new Product();
        product.setName("Product1");

        when(productProvider.save(product)).thenReturn(product);

        Product result = productProvider.save(product);
        assertNotNull(result);
        assertEquals("Product1", result.getName());
    }

    @Test
    public void testDeleteById() {
        doNothing().when(productProvider).deleteById(1L);

        productProvider.deleteById(1L);

        verify(productProvider, times(1)).deleteById(1L);
    }

    @Test
    public void testExisteByName() {
        when(productProvider.existeByName("Product1")).thenReturn(true);

        boolean result = productProvider.existeByName("Product1");
        assertTrue(result);
    }
}