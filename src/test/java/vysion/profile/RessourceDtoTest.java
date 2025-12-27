package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import secop.ppm1.adapters.expose.RessourceDto;

public class RessourceDtoTest {

    @Test
    public void testRessourceDtoInstantiation() {
        // Given & When
        RessourceDto ressource = new RessourceDto();
        
        // Then
        assertNotNull(ressource);
    }

    @Test
    public void testRessourceDtoFields() {
        // Given
        RessourceDto ressource = new RessourceDto();
        
        // When & Then
        // Comme les champs sont package-private, on teste juste l'instantiation
        assertNotNull(ressource);
        // On ne peut pas tester les champs car ils ne sont pas publics
        // Cela démontre la nécessité d'ajouter des getters/setters ou d'utiliser @Data
    }

    @Test
    public void testCriticiteEnumValues() {
        // Test all enum values
        assertEquals("LOW", RessourceDto.Criticite.LOW.name());
        assertEquals("MEDIUM", RessourceDto.Criticite.MEDIUM.name());
        assertEquals("HIGH", RessourceDto.Criticite.HIGH.name());
        
        // Test valueOf
        assertEquals(RessourceDto.Criticite.LOW, RessourceDto.Criticite.valueOf("LOW"));
        assertEquals(RessourceDto.Criticite.MEDIUM, RessourceDto.Criticite.valueOf("MEDIUM"));
        assertEquals(RessourceDto.Criticite.HIGH, RessourceDto.Criticite.valueOf("HIGH"));
        
        // Test values()
        RessourceDto.Criticite[] values = RessourceDto.Criticite.values();
        assertEquals(3, values.length);
        assertTrue(contains(values, RessourceDto.Criticite.LOW));
        assertTrue(contains(values, RessourceDto.Criticite.MEDIUM));
        assertTrue(contains(values, RessourceDto.Criticite.HIGH));
    }
    
    @Test
    public void testCriticiteEnumOrdinal() {
        assertEquals(0, RessourceDto.Criticite.LOW.ordinal());
        assertEquals(1, RessourceDto.Criticite.MEDIUM.ordinal());
        assertEquals(2, RessourceDto.Criticite.HIGH.ordinal());
    }
    
    private boolean contains(RessourceDto.Criticite[] array, RessourceDto.Criticite value) {
        for (RessourceDto.Criticite item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }
}