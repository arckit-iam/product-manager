package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import secop.ppm1.adapters.in.JpaProductProvider;
import secop.ppm1.adapters.in.JpaProductRepository;
import secop.ppm1.adapters.in.ProductEntity;
import secop.ppm1.adapters.in.ProductEntityMapper;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.model.Product;

public class JpaProductProviderTest {

    @Mock
    private JpaProductRepository productRepository;

    @Mock
    private ProductEntityMapper productEntityMapper;

    @InjectMocks
    private JpaProductProvider jpaProductProvider;

    private Product testProduct;
    private ProductEntity testProductEntity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");

        testProductEntity = new ProductEntity();
        testProductEntity.setId(1L);
        testProductEntity.setName("Test Product");
        testProductEntity.setDescription("Test Description");
    }

    @Test
    public void testFindAll() {
        ProductEntity entity1 = new ProductEntity();
        entity1.setName("Product1");
        ProductEntity entity2 = new ProductEntity();
        entity2.setName("Product2");
        
        List<ProductEntity> entities = Arrays.asList(entity1, entity2);

        Product product1 = new Product();
        product1.setName("Product1");
        Product product2 = new Product();
        product2.setName("Product2");
        
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(entities);
        when(productEntityMapper.toDomainList(entities)).thenReturn(products);

        List<Product> result = jpaProductProvider.findAll();

        assertEquals(2, result.size());
        assertEquals("Product1", result.get(0).getName());
        assertEquals("Product2", result.get(1).getName());
        verify(productRepository, times(1)).findAll();
        verify(productEntityMapper, times(1)).toDomainList(entities);
    }

    @Test
    public void testFindById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProductEntity));
        when(productEntityMapper.toDomain(testProductEntity)).thenReturn(testProduct);

        Product result = jpaProductProvider.findById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findById(1L);
        verify(productEntityMapper, times(1)).toDomain(testProductEntity);
    }

    @Test
    public void testFindById_ThrowsNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            jpaProductProvider.findById(1L);
        });

        verify(productRepository, times(1)).findById(1L);
        verify(productEntityMapper, never()).toDomain(any(ProductEntity.class));
    }

    @Test
    public void testFindByProductName() {
        when(productRepository.findByName("Test Product")).thenReturn(testProductEntity);
        when(productEntityMapper.toDomain(testProductEntity)).thenReturn(testProduct);

        Product result = jpaProductProvider.findByProductName("Test Product");

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findByName("Test Product");
        verify(productEntityMapper, times(1)).toDomain(testProductEntity);
    }

    @Test
    public void testSave_NewProduct() {
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(productEntityMapper.toEntity(testProduct)).thenReturn(testProductEntity);
        when(productRepository.saveAndFlush(testProductEntity)).thenReturn(testProductEntity);
        when(productEntityMapper.toDomain(testProductEntity)).thenReturn(testProduct);

        Product result = jpaProductProvider.save(testProduct);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).existsByName("Test Product");
        verify(productEntityMapper, times(1)).toEntity(testProduct);
        verify(productRepository, times(1)).saveAndFlush(testProductEntity);
        verify(productEntityMapper, times(1)).toDomain(testProductEntity);
    }

    @Test
    public void testSave_ExistingProduct() {
        when(productRepository.existsByName("Test Product")).thenReturn(true);
        when(productRepository.findIdByProductName("Test Product")).thenReturn(1L);
        when(productEntityMapper.toEntity(testProduct)).thenReturn(testProductEntity);
        when(productRepository.saveAndFlush(testProductEntity)).thenReturn(testProductEntity);
        when(productEntityMapper.toDomain(testProductEntity)).thenReturn(testProduct);

        Product result = jpaProductProvider.save(testProduct);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).existsByName("Test Product");
        verify(productRepository, times(1)).findIdByProductName("Test Product");
        verify(productEntityMapper, times(1)).toEntity(testProduct);
        verify(productRepository, times(1)).saveAndFlush(testProductEntity);
        verify(productEntityMapper, times(1)).toDomain(testProductEntity);
    }

    @Test
    public void testDeleteById() {
        doNothing().when(productRepository).deleteById(1L);

        jpaProductProvider.deleteById(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteByName() {
        when(productRepository.findIdByProductName("Test Product")).thenReturn(1L);
        doNothing().when(productRepository).deleteById(1L);

        jpaProductProvider.deleteByName("Test Product");

        verify(productRepository, times(1)).findIdByProductName("Test Product");
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testExisteByName_True() {
        when(productRepository.existsByName("Test Product")).thenReturn(true);

        boolean result = jpaProductProvider.existeByName("Test Product");

        assertTrue(result);
        verify(productRepository, times(1)).existsByName("Test Product");
    }

    @Test
    public void testExisteByName_False() {
        when(productRepository.existsByName("Non Existing Product")).thenReturn(false);

        boolean result = jpaProductProvider.existeByName("Non Existing Product");

        assertFalse(result);
        verify(productRepository, times(1)).existsByName("Non Existing Product");
    }
}