package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import secop.ppm1.adapters.in.ProductEntity;
import secop.ppm1.adapters.in.ProductEntityMapper;
import secop.ppm1.domain.model.Product;

public class ProductEntityMapperTest {

    private ProductEntityMapper productEntityMapper;
    private Product product;
    private ProductEntity productEntity;

    @BeforeEach
    public void setUp() {
        productEntityMapper = Mappers.getMapper(ProductEntityMapper.class);
        
        product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");

        productEntity = new ProductEntity();
        productEntity.setId(1L);
        productEntity.setName("Test Product");
        productEntity.setDescription("Test Description");
    }

    @Test
    public void testToDomain() {
        Product result = productEntityMapper.toDomain(productEntity);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    public void testToDomain_NullEntity() {
        Product result = productEntityMapper.toDomain(null);
        assertNull(result);
    }

    @Test
    public void testToEntity() {
        ProductEntity result = productEntityMapper.toEntity(product);

        assertNotNull(result);
        assertNull(result.getId()); // ID est ignoré lors du mapping
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    public void testToEntity_NullDomain() {
        ProductEntity result = productEntityMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    public void testToDomainList() {
        ProductEntity entity1 = new ProductEntity();
        entity1.setId(1L);
        entity1.setName("Product1");
        entity1.setDescription("Description1");

        ProductEntity entity2 = new ProductEntity();
        entity2.setId(2L);
        entity2.setName("Product2");
        entity2.setDescription("Description2");

        List<ProductEntity> entities = Arrays.asList(entity1, entity2);

        List<Product> result = productEntityMapper.toDomainList(entities);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product1", result.get(0).getName());
        assertEquals("Description1", result.get(0).getDescription());
        assertEquals("Product2", result.get(1).getName());
        assertEquals("Description2", result.get(1).getDescription());
    }

    @Test
    public void testToDomainList_EmptyList() {
        List<ProductEntity> emptyList = Arrays.asList();
        List<Product> result = productEntityMapper.toDomainList(emptyList);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testToDomainList_NullList() {
        List<Product> result = productEntityMapper.toDomainList(null);
        assertNull(result);
    }

    @Test
    public void testToEntityList() {
        Product domain1 = new Product();
        domain1.setName("Product1");
        domain1.setDescription("Description1");

        Product domain2 = new Product();
        domain2.setName("Product2");
        domain2.setDescription("Description2");

        List<Product> domains = Arrays.asList(domain1, domain2);

        List<ProductEntity> result = productEntityMapper.toEntityList(domains);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product1", result.get(0).getName());
        assertEquals("Description1", result.get(0).getDescription());
        assertNull(result.get(0).getId()); // ID ignoré
        assertEquals("Product2", result.get(1).getName());
        assertEquals("Description2", result.get(1).getDescription());
        assertNull(result.get(1).getId()); // ID ignoré
    }

    @Test
    public void testToEntityList_EmptyList() {
        List<Product> emptyList = Arrays.asList();
        List<ProductEntity> result = productEntityMapper.toEntityList(emptyList);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testToEntityList_NullList() {
        List<ProductEntity> result = productEntityMapper.toEntityList(null);
        assertNull(result);
    }
}