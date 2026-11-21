// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import secop.ppm1.domain.model.TypeRessource;

public class TypeRessourceTest {

    private TypeRessource typeRessource;

    @BeforeEach
    public void setUp() {
        typeRessource = new TypeRessource();
    }

    @Test
    public void testSetAndGetName() {
        String name = "DATABASE_RESOURCE";
        typeRessource.setName(name);
        assertEquals(name, typeRessource.getName());
    }

    @Test
    public void testSetAndGetLevel() {
        int level = 7;
        typeRessource.setLevel(level);
        assertEquals(level, typeRessource.getLevel());
    }

    @Test
    public void testTypeRessourceEqualsAndHashCode() {
        TypeRessource type1 = new TypeRessource();
        type1.setName("API_RESOURCE");
        type1.setLevel(5);
        
        TypeRessource type2 = new TypeRessource();
        type2.setName("API_RESOURCE");
        type2.setLevel(5);
        
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    public void testTypeRessourceToString() {
        typeRessource.setName("FILE_RESOURCE");
        typeRessource.setLevel(2);
        
        String toString = typeRessource.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FILE_RESOURCE"));
        assertTrue(toString.contains("2"));
    }

    @Test
    public void testEmptyTypeRessource() {
        assertNull(typeRessource.getName());
        assertEquals(0, typeRessource.getLevel()); // int primitive par défaut est 0
    }

    @Test
    public void testDifferentResourceTypes() {
        String[] resourceTypes = {
            "DATABASE",
            "API",
            "FILE_SYSTEM",
            "NETWORK",
            "SERVICE",
            "WEB_SERVICE"
        };
        
        for (String resourceType : resourceTypes) {
            typeRessource.setName(resourceType);
            assertEquals(resourceType, typeRessource.getName());
        }
    }

    @Test
    public void testNegativeAndPositiveLevels() {
        typeRessource.setName("TEST_RESOURCE");
        
        // Test avec différents niveaux
        typeRessource.setLevel(-10);
        assertEquals(-10, typeRessource.getLevel());
        
        typeRessource.setLevel(0);
        assertEquals(0, typeRessource.getLevel());
        
        typeRessource.setLevel(1000);
        assertEquals(1000, typeRessource.getLevel());
    }
}