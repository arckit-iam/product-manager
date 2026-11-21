// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import secop.ppm1.domain.model.TypePermission;

public class TypePermissionTest {

    private TypePermission typePermission;

    @BeforeEach
    public void setUp() {
        typePermission = new TypePermission();
    }

    @Test
    public void testSetAndGetName() {
        String name = "READ_PERMISSION";
        typePermission.setName(name);
        assertEquals(name, typePermission.getName());
    }

    @Test
    public void testSetAndGetLevel() {
        int level = 5;
        typePermission.setLevel(level);
        assertEquals(level, typePermission.getLevel());
    }

    @Test
    public void testTypePermissionEqualsAndHashCode() {
        TypePermission type1 = new TypePermission();
        type1.setName("ADMIN");
        type1.setLevel(10);
        
        TypePermission type2 = new TypePermission();
        type2.setName("ADMIN");
        type2.setLevel(10);
        
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    public void testTypePermissionToString() {
        typePermission.setName("WRITE_PERMISSION");
        typePermission.setLevel(3);
        
        String toString = typePermission.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("WRITE_PERMISSION"));
        assertTrue(toString.contains("3"));
    }

    @Test
    public void testEmptyTypePermission() {
        assertNull(typePermission.getName());
        assertEquals(0, typePermission.getLevel()); // int primitive par défaut est 0
    }

    @Test
    public void testDifferentLevels() {
        typePermission.setName("TEST_PERMISSION");
        
        // Test avec différents niveaux
        typePermission.setLevel(0);
        assertEquals(0, typePermission.getLevel());
        
        typePermission.setLevel(1);
        assertEquals(1, typePermission.getLevel());
        
        typePermission.setLevel(100);
        assertEquals(100, typePermission.getLevel());
        
        typePermission.setLevel(-1);
        assertEquals(-1, typePermission.getLevel());
    }
}