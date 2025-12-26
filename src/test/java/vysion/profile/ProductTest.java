package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.Ressource;

public class ProductTest {

    private Product product;

    @BeforeEach
    public void setUp() {
        product = new Product();
    }

    @Test
    public void testSetAndGetName() {
        String name = "Test Product";
        product.setName(name);
        assertEquals(name, product.getName());
    }

    @Test
    public void testSetAndGetDescription() {
        String description = "Test Description";
        product.setDescription(description);
        assertEquals(description, product.getDescription());
    }

    @Test
    public void testSetAndGetProfiles() {
        Profile profile1 = new Profile();
        profile1.setName("Profile1");
        
        Profile profile2 = new Profile();
        profile2.setName("Profile2");
        
        List<Profile> profiles = Arrays.asList(profile1, profile2);
        product.setProfiles(profiles);
        
        assertEquals(2, product.getProfiles().size());
        assertEquals("Profile1", product.getProfiles().get(0).getName());
        assertEquals("Profile2", product.getProfiles().get(1).getName());
    }

    @Test
    public void testSetAndGetRessources() {
        Ressource ressource1 = new Ressource();
        ressource1.setName("Resource1");
        
        Ressource ressource2 = new Ressource();
        ressource2.setName("Resource2");
        
        List<Ressource> ressources = Arrays.asList(ressource1, ressource2);
        product.setRessources(ressources);
        
        assertEquals(2, product.getRessources().size());
        assertEquals("Resource1", product.getRessources().get(0).getName());
        assertEquals("Resource2", product.getRessources().get(1).getName());
    }

    @Test
    public void testProductEqualsAndHashCode() {
        Product product1 = new Product();
        product1.setName("Test");
        product1.setDescription("Desc");
        
        Product product2 = new Product();
        product2.setName("Test");
        product2.setDescription("Desc");
        
        assertEquals(product1, product2);
        assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    public void testProductToString() {
        product.setName("Test Product");
        product.setDescription("Test Description");
        
        String toString = product.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Product"));
        assertTrue(toString.contains("Test Description"));
    }
}