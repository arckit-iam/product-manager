package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import secop.ppm1.domain.model.Profile;

public class ProfileTest {

    private Profile profile;

    @BeforeEach
    public void setUp() {
        profile = new Profile();
    }

    @Test
    public void testSetAndGetProduct() {
        String product = "Test Product";
        profile.setProduct(product);
        assertEquals(product, profile.getProduct());
    }

    @Test
    public void testSetAndGetName() {
        String name = "Test Profile";
        profile.setName(name);
        assertEquals(name, profile.getName());
    }

    @Test
    public void testSetAndGetDescription() {
        String description = "Test Description";
        profile.setDescription(description);
        assertEquals(description, profile.getDescription());
    }

    @Test
    public void testSetAndGetScope() {
        String scope = "TIER1";
        profile.setScope(scope);
        assertEquals(scope, profile.getScope());
    }

    @Test
    public void testProfileEqualsAndHashCode() {
        Profile profile1 = new Profile();
        profile1.setProduct("Product1");
        profile1.setName("Profile1");
        profile1.setDescription("Desc1");
        profile1.setScope("TIER1");
        
        Profile profile2 = new Profile();
        profile2.setProduct("Product1");
        profile2.setName("Profile1");
        profile2.setDescription("Desc1");
        profile2.setScope("TIER1");
        
        assertEquals(profile1, profile2);
        assertEquals(profile1.hashCode(), profile2.hashCode());
    }

    @Test
    public void testProfileToString() {
        profile.setProduct("Test Product");
        profile.setName("Test Profile");
        profile.setDescription("Test Description");
        profile.setScope("TIER2");
        
        String toString = profile.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Product"));
        assertTrue(toString.contains("Test Profile"));
        assertTrue(toString.contains("Test Description"));
        assertTrue(toString.contains("TIER2"));
    }

    @Test
    public void testEmptyProfile() {
        assertNull(profile.getProduct());
        assertNull(profile.getName());
        assertNull(profile.getDescription());
        assertNull(profile.getScope());
    }
}