package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Ressource;
import secop.ppm1.domain.model.TypeRessource;

public class RessourceTest {

    private Ressource ressource;
    private TypeRessource typeRessource;
    private Product product;

    @BeforeEach
    public void setUp() {
        ressource = new Ressource();
        typeRessource = new TypeRessource();
        product = new Product();
    }

    @Test
    public void testSetAndGetName() {
        String name = "Test Resource";
        ressource.setName(name);
        assertEquals(name, ressource.getName());
    }

    @Test
    public void testSetAndGetTypeRessource() {
        typeRessource.setName("Database");
        typeRessource.setLevel(3);
        
        ressource.setTypeRessource(typeRessource);
        
        assertEquals(typeRessource, ressource.getTypeRessource());
        assertEquals("Database", ressource.getTypeRessource().getName());
        assertEquals(3, ressource.getTypeRessource().getLevel());
    }

    @Test
    public void testSetAndGetProduct() {
        product.setName("Test Product");
        product.setDescription("Test Description");
        
        ressource.setProduct(product);
        
        assertEquals(product, ressource.getProduct());
        assertEquals("Test Product", ressource.getProduct().getName());
        assertEquals("Test Description", ressource.getProduct().getDescription());
    }

    @Test
    public void testCriticiteEnum() {
        // Test des valeurs de l'énumération Criticite
        assertEquals("LOW", Ressource.Criticite.LOW.name());
        assertEquals("MEDIUM", Ressource.Criticite.MEDIUM.name());
        assertEquals("HIGH", Ressource.Criticite.HIGH.name());
        
        // Test des valeurs ordonnées
        assertTrue(Ressource.Criticite.LOW.ordinal() < Ressource.Criticite.MEDIUM.ordinal());
        assertTrue(Ressource.Criticite.MEDIUM.ordinal() < Ressource.Criticite.HIGH.ordinal());
    }

    @Test
    public void testRessourceEqualsAndHashCode() {
        TypeRessource type1 = new TypeRessource();
        type1.setName("API");
        type1.setLevel(2);
        
        Product product1 = new Product();
        product1.setName("Product1");
        
        Ressource ressource1 = new Ressource();
        ressource1.setName("Resource1");
        ressource1.setTypeRessource(type1);
        ressource1.setProduct(product1);
        
        Ressource ressource2 = new Ressource();
        ressource2.setName("Resource1");
        ressource2.setTypeRessource(type1);
        ressource2.setProduct(product1);
        
        assertEquals(ressource1, ressource2);
        assertEquals(ressource1.hashCode(), ressource2.hashCode());
    }

    @Test
    public void testRessourceToString() {
        typeRessource.setName("Service");
        typeRessource.setLevel(1);
        
        product.setName("Test Product");
        
        ressource.setName("Test Resource");
        ressource.setTypeRessource(typeRessource);
        ressource.setProduct(product);
        
        String toString = ressource.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Resource"));
    }

    @Test
    public void testEmptyRessource() {
        assertNull(ressource.getName());
        assertNull(ressource.getTypeRessource());
        assertNull(ressource.getProduct());
    }
}